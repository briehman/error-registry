package com.briehman.errorregistry.interactor

import java.time.LocalDateTime

import com.briehman.errorregistry.boundary.{ErrorOccurrenceSummary, ErrorSummary, GetErrorSummaryBoundary}
import com.briehman.errorregistry.repository.{ErrorOccurrenceRepository, ErrorRepository}

class GetErrorSummaryInteractor(errorRepository: ErrorRepository,
                                failureOccurrenceRepository: ErrorOccurrenceRepository)
  extends GetErrorSummaryBoundary {

  override def getUniqueRecentOccurrenceSummaries(since: LocalDateTime, max: Int): Seq[ErrorSummary] = {
    mapToFailureOccurrences(failureOccurrenceRepository.listUniqueRecentOccurrences(since, max))
  }

  override def getMostFrequentRecentOccurrencesSummaries(since: LocalDateTime, max: Int): Seq[ErrorSummary] = {
    mapToFailureOccurrences(failureOccurrenceRepository.listMostFrequentOccurrences(since, max))
  }

  private def mapToFailureOccurrences(occurrences: Seq[ErrorOccurrenceSummary]): Seq[ErrorSummary] = {
    occurrences
      .flatMap { o =>
        errorRepository.find(o.error_pk).map { f =>
          ErrorSummary(f, o)
        }
      }
  }
}
