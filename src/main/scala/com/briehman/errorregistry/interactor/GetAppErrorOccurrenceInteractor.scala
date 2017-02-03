package com.briehman.errorregistry.interactor

import com.briehman.errorregistry.boundary.GetAppErrorOccurrenceBoundary
import com.briehman.errorregistry.models.ErrorOccurrence
import com.briehman.errorregistry.repository.ErrorOccurrenceRepository

class GetAppErrorOccurrenceInteractor(errorOccurrenceRepository: ErrorOccurrenceRepository)
  extends GetAppErrorOccurrenceBoundary {
  override def getOccurrences(appErrorId: Int, startAt: Int, maxResults: Int): Seq[ErrorOccurrence] = {
    errorOccurrenceRepository.findByError(appErrorId, startAt, maxResults)
  }
}
