package com.briehman.errorregistry.interactor

import java.time.LocalDateTime

import com.briehman.errorregistry.boundary.{ErrorOccurrenceSummary, ErrorSummary, GetErrorSummaryBoundary}
import com.briehman.errorregistry.repository.{ErrorOccurrenceRepository, ErrorRepository}

class GetErrorSummaryInteractor(errorOccurrenceRepository: ErrorOccurrenceRepository)
  extends GetErrorSummaryBoundary {

  override def listNew(since: LocalDateTime, max: Int): Seq[ErrorSummary] = {
    errorOccurrenceRepository.listUniqueNew(since, max)
  }

  override def listRecent(since: LocalDateTime, max: Int): Seq[ErrorSummary] = {
    errorOccurrenceRepository.listUniqueRecent(since, max)
  }

  override def listMostFrequent(since: LocalDateTime, max: Int): Seq[ErrorSummary] = {
    errorOccurrenceRepository.listUniqueMostFrequent(since, max)
  }
}
