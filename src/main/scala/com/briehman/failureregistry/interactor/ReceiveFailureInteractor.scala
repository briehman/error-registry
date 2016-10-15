package com.briehman.failureregistry.interactor

import com.briehman.failureregistry.boundary.{ReceiveFailed, ReceiveFailureBoundary, ReceiveFailureResponse, ReceivedOk}
import com.briehman.failureregistry.message.FailureMessage
import com.briehman.failureregistry.models.{FailureOccurrence, Failure}
import com.briehman.failureregistry.repository.{FailureOccurrenceRepository, FailureRepository}
import com.briehman.failureregistry.service.NotificationService

class ReceiveFailureInteractor(failureRepository: FailureRepository,
                               occurrenceRepository: FailureOccurrenceRepository,
                               notificationService: NotificationService) extends ReceiveFailureBoundary {
  override def receiveFailure(message: FailureMessage): ReceiveFailureResponse = {
    if (message == null) {
      ReceiveFailed
    } else {
      val failure = storeFailure(Failure(message))
      val occurrence = FailureOccurrence(message, failure)
      occurrenceRepository.store(occurrence)
      ReceivedOk(failure)
    }
  }

  def storeFailure(failure: Failure): Failure = {
    val existingFailure = failureRepository.find(failure.code)
    val returnFailure = existingFailure match {
      case None =>
        notificationService.notify(failure)
        failureRepository.store(failure)
      case Some(f) => f
    }
    returnFailure
  }
}
