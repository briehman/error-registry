package com.briehman.errorregistry.repository.db

import java.sql.Timestamp
import java.time.LocalDateTime

import com.briehman.errorregistry.boundary.{AppErrorDetailStats, ErrorOccurrenceSummary, ErrorSummary}
import com.briehman.errorregistry.models.ErrorOccurrence
import com.briehman.errorregistry.repository.ErrorOccurrenceRepository
import slick.driver.MySQLDriver.api._

import scala.concurrent.Await
import scala.concurrent.duration.Duration

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
    Await.result(db.run(insert += occurrence), Duration.Inf)
  }

  override def listUniqueNew(since: LocalDateTime, max: Int): Seq[ErrorSummary] = {
    val uniqueNewErrorsSince = getErrorsSinceWithRecentCount(since)
      .filter { case (_, _, minDate, _, _, _) => minDate >= Timestamp.valueOf(since) }
      .sortBy { case (_, _, minDate, _, _, _) => minDate.desc }
      .take(max)

    Await.result(db.run(uniqueNewErrorsSince.result), Duration.Inf).map { case (errorId, code, minDate, maxDate, sinceErrorCount, totalErrorCount) =>
      ErrorSummary(errorId, code, ErrorOccurrenceSummary(errorId, minDate.get.toLocalDateTime, maxDate.get.toLocalDateTime, sinceErrorCount, totalErrorCount))
    }
  }

  override def listUniqueRecent(since: LocalDateTime, max: Int): Seq[ErrorSummary] = {
    val uniqueErrorsSince = getErrorsSinceWithRecentCount(since)
      .sortBy { case (_, _, minDate, _, _, _) => minDate.desc }
      .take(max)

    Await.result(db.run(uniqueErrorsSince.result), Duration.Inf).map { case (errorId, code, minDate, maxDate, sinceErrorCount, totalErrorCount) =>
      ErrorSummary(errorId, code, ErrorOccurrenceSummary(errorId, minDate.get.toLocalDateTime, maxDate.get.toLocalDateTime, sinceErrorCount, totalErrorCount))
    }
  }

  override def listUniqueMostFrequent(since: LocalDateTime, max: Int): Seq[ErrorSummary] = {
    val uniqueErrorsSince = getErrorsSinceWithRecentCount(since)
      .sortBy { case (_, _, _, _, recentErrorCount, totalErrorCount) => (recentErrorCount.desc, totalErrorCount.desc) }
      .take(max)

    Await.result(db.run(uniqueErrorsSince.result), Duration.Inf).map { case (errorId, code, minDate, maxDate, sinceErrorCount, totalErrorCount) =>
      ErrorSummary(errorId, code,
        ErrorOccurrenceSummary(errorId, minDate.get.toLocalDateTime, maxDate.get.toLocalDateTime,
          sinceErrorCount, totalErrorCount))
    }
  }


  override def getStatsByAppError(errorId: Int): Option[AppErrorDetailStats] = {
    val errorStats = (for {
      occurrences <- ErrorOccurrence.table
        .filter(_.errorId === errorId)

    } yield occurrences)
      .groupBy(_.errorId)
      .map { case (_, group) =>
        (group.map(_.date).min.get, group.map(_.date).max.get, group.length)
      }

    Await.result(db.run(errorStats.result), Duration.Inf).headOption.map { case (minDate, maxDate, totalErrorOccurrenceCount) =>
      AppErrorDetailStats(minDate.toLocalDateTime, maxDate.toLocalDateTime, totalErrorOccurrenceCount)
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
      .groupBy { case (error, _, recentCount) => (error.id, error.code, recentCount) }
      .map { case ((errorId, code, recentCount), errorResults) =>
        (errorId, code, errorResults.map(_._2.date).min, errorResults.map(_._2.date).max,
          recentCount, errorResults.length)
      }
  }
}
