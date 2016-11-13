package com.briehman.failureregistry.interactor

import java.time.LocalDateTime

import com.briehman.failureregistry.boundary.{FailureOccurrenceSummary, FailureSummary, GetFailureSummaryBoundary}
import com.briehman.failureregistry.models.Failure
import com.briehman.failureregistry.repository.{FailureOccurrenceRepository, FailureRepository}

class GetFailureSummaryInteractor(failureRepository: FailureRepository,
                                  failureOccurrenceRepository: FailureOccurrenceRepository)
  extends GetFailureSummaryBoundary {

  override def findFailure(code: String): Option[Failure] = {
    failureRepository.find(code)
  }

  override def getUniqueRecentOccurrenceSummaries(since: LocalDateTime, max: Int): Seq[FailureSummary] = {
    mapToFailureOccurrences(failureOccurrenceRepository
      .listUniqueRecentOccurrences(since, max))
  }

  override def getTopRecentOccurrencesSummaries(since: LocalDateTime, max: Int): Seq[FailureSummary] = {
    mapToFailureOccurrences(failureOccurrenceRepository
      .listTopOccurrences(since, max))
  }

  private def mapToFailureOccurrences(occurrences: Seq[FailureOccurrenceSummary]): Seq[FailureSummary] = {
    occurrences
      .flatMap { o =>
        failureRepository.find(o.failure_pk).map { f =>
          FailureSummary(f, o)
        }
      }
  }
}