package com.briehman.errorregistry.repository

import java.sql.Timestamp
import java.time.LocalDateTime

import slick.driver.MySQLDriver.api._
import com.briehman.errorregistry.boundary.{ErrorOccurrenceSummary, ErrorSummary}
import com.briehman.errorregistry.models.{AppError, ErrorOccurrence}
import slick.lifted.QueryBase

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class InMemoryErrorRepository extends ErrorRepository {
  private var codes = Map[String, AppError]()

  override def find(primaryKey: Int): Option[AppError] = codes.values.find(_.id == primaryKey)

  override def find(code: String): Option[AppError] = codes.get(code)

  override def list(ids: Seq[Int]): Seq[AppError] = {
    codes.values.filter(ids contains _.id).toSeq
  }

  override def store(error: AppError): AppError = {
    if (codes.contains(error.code)) {
      throw new IllegalArgumentException("Cannot double persist")
    }
    val storedError = error.copy(id = codes.size)
    codes = codes + (error.code -> storedError)
    storedError
  }

  override def listCodes: Seq[String] = codes.keys.toSeq
}

class DatabaseErrorRepository(db: Database) extends ErrorRepository {
  override def find(id: Int): Option[AppError] = {
    Await.result(db.run(AppError.table.filter(_.id === id).result.headOption), Duration.Inf)
  }

  override def find(code: String): Option[AppError] = {
    Await.result(db.run(AppError.table.filter(_.code === code).result.headOption), Duration.Inf)
  }

  override def list(ids: Seq[Int]): Seq[AppError] = {
    Await.result(db.run(AppError.table.result), Duration.Inf)
  }

  override def store(error: AppError): AppError = {
    Await.result(db.run(AppError.table += error), Duration.Inf)
    find(error.code).head
  }

  override def listCodes: Seq[String] = {
    Await.result(db.run(AppError.table.map(_.code).result), Duration.Inf)
  }
}

class InMemoryErrorOccurrenceRepository(errorRepository: ErrorRepository)
  extends ErrorOccurrenceRepository {
  private var occurrencesById = Map[Int, ErrorOccurrence]()

  override def find(id: Int): Option[ErrorOccurrence] = occurrencesById.get(id)

  override def findByCode(code: String): Seq[ErrorOccurrence] = {
    errorRepository.find(code) match {
      case None => List()
      case Some(f) => findById(f.id)
    }
  }

  private def findById(id: Int) = {
    occurrencesById.filter(t => t._2.error_pk == id).values.toList
  }

  override def store(occurrence: ErrorOccurrence): ErrorOccurrence = {
    val stored = occurrence.copy(id = occurrencesById.size)
    occurrencesById = occurrencesById + (stored.id -> stored)
    stored
  }

  override def listUniqueNew(since: LocalDateTime, max: Int): Seq[ErrorSummary] = {
    getOccurrenceSummariesSince(since)
      .filter(o => o.firstSeen.isAfter(since))
      .sortBy(_.firstSeen)(Ordering[LocalDateTime].reverse)
      .take(max)
      .map { t =>
        val error = errorRepository.find(t.error_pk)
        ErrorSummary(t.error_pk, error.get.code, t)
      }
  }

  override def listUniqueRecent(since: LocalDateTime, max: Int): Seq[ErrorSummary] = {
    getOccurrenceSummariesSince(since)
      .sortBy(_.lastSeen)(Ordering[LocalDateTime].reverse)
      .take(max)
      .map { t =>
        val error = errorRepository.find(t.error_pk)
        ErrorSummary(t.error_pk, error.get.code, t)
      }
  }

  override def listUniqueMostFrequent(since: LocalDateTime, max: Int): Seq[ErrorSummary] = {
    getOccurrenceSummariesSince(since)
      .sortBy(_.totalOccurrences)(Ordering[Int].reverse)
      .take(max)
      .map { t =>
        val error = errorRepository.find(t.error_pk)
        ErrorSummary(t.error_pk, error.get.code, t)
      }
  }

  private def getOccurrenceSummariesSince(date: LocalDateTime): List[ErrorOccurrenceSummary] = {
    occurrencesById
      .filter { case (id, occurrence) => occurrence.date.toLocalDateTime.isAfter(date) }
      .groupBy { case (id, occurrence) => occurrence.error_pk }
      .map { case (id, occurrenceMap) =>
        val errorOccurrences = occurrencesById.values.filter(_.error_pk == id)
        val firstSeen = errorOccurrences.minBy(_.date).date
        val lastSeen = errorOccurrences.maxBy(_.date).date
        ErrorOccurrenceSummary(id, firstSeen.toLocalDateTime, lastSeen.toLocalDateTime, errorOccurrences.size, occurrencesById.values.count(_.error_pk == id))
      }
      .toList
  }
}

class DatabaseErrorOccurrenceRepository(db: Database) extends ErrorOccurrenceRepository {
  override def find(id: Int): Option[ErrorOccurrence] = {
    Await.result(db.run(ErrorOccurrence.table.filter(_.id === id).result.headOption), Duration.Inf)
  }

  override def findByCode(code: String): Seq[ErrorOccurrence] = {
    val occurrences = for {
      o <- ErrorOccurrence.table
      e <- o.appError if e.code === code
    } yield o

    Await.result(db.run(occurrences.result), Duration.Inf)
  }

  override def store(occurrence: ErrorOccurrence): ErrorOccurrence = {
    val insert = ErrorOccurrence.table returning ErrorOccurrence.table.map(_.id) into ((item, id) => item.copy(id = id))
    val id = Await.result(db.run(ErrorOccurrence.table += occurrence), Duration.Inf)
    find(id).head
  }

  override def listUniqueNew(since: LocalDateTime, max: Int): Seq[ErrorSummary] = {
    val uniqueNewErrorsSince = getErrorsSinceWithRecentCount(since)
      .filter { case (_, minDate, _, _, _) => minDate >= Timestamp.valueOf(since) }
      .sortBy { case (_, minDate, _, _, _) => minDate.desc }
      .take(max)

    Await.result(db.run(uniqueNewErrorsSince.result), Duration.Inf).map { case (errorId, minDate, maxDate, sinceErrorCount, totalErrorCount) =>
      ErrorSummary(1, "FAKE", ErrorOccurrenceSummary(errorId, minDate.get.toLocalDateTime, maxDate.get.toLocalDateTime, sinceErrorCount, totalErrorCount))
    }
  }

  override def listUniqueRecent(since: LocalDateTime, max: Int): Seq[ErrorSummary] = {
    val uniqueErrorsSince = getErrorsSinceWithRecentCount(since)
      .filter { case (_, minDate, _, _, _) => minDate >= Timestamp.valueOf(since) }
      .sortBy { case (_, minDate, _, _, _) => minDate.desc }
      .take(max)

    Await.result(db.run(uniqueErrorsSince.result), Duration.Inf).map { case (errorId, minDate, maxDate, sinceErrorCount, totalErrorCount) =>
      ErrorSummary(1, "FAKE", ErrorOccurrenceSummary(errorId, minDate.get.toLocalDateTime, maxDate.get.toLocalDateTime, sinceErrorCount, totalErrorCount))
    }
  }

  private def getErrorsSinceWithRecentCount(since: LocalDateTime) = {
    (for {
      (recent, all) <- ErrorOccurrence.table
        .filter(_.date >= Timestamp.valueOf(since))
        .groupBy(_.errorId)
        .map { case (errorId, group) => (errorId, group.length) }
        .join(ErrorOccurrence.table) on (_._1 === _.errorId)
      e <- all.appError
    } yield (e, all, recent._2))
      .groupBy(_._1.id)
      .map { case (errorId, errorResults) =>
        (errorId, errorResults.map(_._2.date).min, errorResults.map(_._2.date).max, errorResults.map(_._3).length, errorResults.length)
      }
  }

  override def listUniqueMostFrequent(since: LocalDateTime, max: Int): Seq[ErrorSummary] = {
    val uniqueErrorsSince = getErrorsSinceWithRecentCount(since)
      .sortBy { case (_, _, _, _, totalErrorCount) => totalErrorCount.desc }
      .take(max)

    Await.result(db.run(uniqueErrorsSince.result), Duration.Inf).map { case (errorId, minDate, maxDate, sinceErrorCount, totalErrorCount) =>
      ErrorSummary(1, "FAKE", ErrorOccurrenceSummary(errorId, minDate.get.toLocalDateTime, maxDate.get.toLocalDateTime, sinceErrorCount, totalErrorCount))
    }
  }

  private def getUniqueErrorsSince(since: LocalDateTime) = {
    val distinctErrors = for {
      o <- ErrorOccurrence.table if o.date >= Timestamp.valueOf(since)
      e <- o.appError
    } yield e.id

    val uniqueErrorsSince = (for {
      o <- ErrorOccurrence.table if o.errorId in distinctErrors
    } yield o)
      .groupBy(_.errorId)
      .map { case (errorId, occurrences) =>
        (errorId, occurrences.map(_.date).min, occurrences.map(_.date).max, occurrences.length)
      }
    uniqueErrorsSince
  }

  case class ErrorOccurrenceSummaryResult(errorId: Rep[Int],
                                          firstOccurrenceDate: Rep[Option[Timestamp]],
                                          latestOccurrenceDate: Rep[Option[Timestamp]],
                                          errorCountSince: Rep[Int])
}
