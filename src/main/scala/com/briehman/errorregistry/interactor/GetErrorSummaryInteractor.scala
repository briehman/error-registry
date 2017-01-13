package com.briehman.errorregistry.interactor

import java.time.LocalDateTime

import com.briehman.errorregistry.boundary.{ErrorOccurrenceSummary, ErrorSummary, GetErrorSummaryBoundary}
import com.briehman.errorregistry.repository.{ErrorOccurrenceRepository, ErrorRepository}

class GetErrorSummaryInteractor(errorRepository: ErrorRepository,
                                failureOccurrenceRepository: ErrorOccurrenceRepository)
  extends GetErrorSummaryBoundary {

  override def listNew(since: LocalDateTime, max: Int): Seq[ErrorSummary] = {
    mapToFailureOccurrences(failureOccurrenceRepository.listUniqueNew(since, max))
  }

  override def listRecent(since: LocalDateTime, max: Int): Seq[ErrorSummary] = {
    mapToFailureOccurrences(failureOccurrenceRepository.listUniqueRecent(since, max))
  }

  override def listMostFrequent(since: LocalDateTime, max: Int): Seq[ErrorSummary] = {
    mapToFailureOccurrences(failureOccurrenceRepository.listUniqueMostFrequent(since, max))
  }

  private def mapToFailureOccurrences(occurrences: Seq[ErrorOccurrenceSummary]): Seq[ErrorSummary] = {
    occurrences
      .flatMap { o =>
        errorRepository.find(o.error_pk).map { f =>
          ErrorSummary(f, o, o.error_pk)
        }
      }
  }
}
