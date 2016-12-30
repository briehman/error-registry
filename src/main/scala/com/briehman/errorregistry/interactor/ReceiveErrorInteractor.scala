package com.briehman.errorregistry.interactor

import com.briehman.errorregistry.boundary.{ReceiveErrorBoundary, ReceiveErrorResponse, ReceiveFailed, ReceivedOk}
import com.briehman.errorregistry.message.AppErrorMessage
import com.briehman.errorregistry.models.{AppError, ErrorOccurrence}
import com.briehman.errorregistry.repository.{ErrorOccurrenceRepository, ErrorRepository}
import com.briehman.errorregistry.service.NotificationService

class ReceiveErrorInteractor(errorRepository: ErrorRepository,
                             occurrenceRepository: ErrorOccurrenceRepository,
                             notificationService: NotificationService) extends ReceiveErrorBoundary {
  override def receiveError(message: AppErrorMessage): ReceiveErrorResponse = {
    if (message == null) {
      ReceiveFailed
    } else {
      val error = storeError(AppError(message))
      val occurrence = ErrorOccurrence(message, error)
      occurrenceRepository.store(occurrence)
      ReceivedOk(error, occurrence)
    }
  }

  private def storeError(error: AppError): AppError = {
    val existingError = errorRepository.find(error.code)
    val returnError = existingError match {
      case None =>
        notificationService.notify(error)
        errorRepository.store(error)
      case Some(e) => e
    }
    returnError
  }
}
