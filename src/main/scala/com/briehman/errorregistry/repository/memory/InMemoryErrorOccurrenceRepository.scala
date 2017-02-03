package com.briehman.errorregistry.repository.memory

import java.time.LocalDateTime

import com.briehman.errorregistry.boundary.{AppErrorDetailStats, ErrorOccurrenceSummary, ErrorSummary}
import com.briehman.errorregistry.models.ErrorOccurrence
import com.briehman.errorregistry.repository.{ErrorOccurrenceRepository, ErrorRepository}

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

  override def getStatsByAppError(errorId: Int): Option[AppErrorDetailStats] = {
    val errorOccurrences = occurrencesById
      .filter { case (id, occurrence) => occurrence.error_pk == errorId }
      .map { case (_, occurrence) => occurrence }

    if (errorOccurrences.nonEmpty) {
      Some(AppErrorDetailStats(
        errorOccurrences.minBy(_.date).date.toLocalDateTime,
        errorOccurrences.maxBy(_.date).date.toLocalDateTime,
        errorOccurrences.size))
    } else {
      None
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

  override def findByError(appErrorId: Int, startAt: Int, maxResults: Int): Seq[ErrorOccurrence] = ???

  override def countErrorOccurrences(appErrorId: Int): Int = ???
}
