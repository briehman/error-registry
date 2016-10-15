package com.briehman.failureregistry.interactor

import com.briehman.failureregistry.boundary.ListFailuresBoundary
import com.briehman.failureregistry.models.Failure
import com.briehman.failureregistry.repository.FailureRepository

class ListFailuresInteractor(failureRepository: FailureRepository) extends ListFailuresBoundary {
  override def findFailure(code: String): Option[Failure] = {
    failureRepository.find(code)
  }
}
