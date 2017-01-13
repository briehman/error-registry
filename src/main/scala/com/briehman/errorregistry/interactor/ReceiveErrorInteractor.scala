package com.briehman.errorregistry.interactor

import java.sql.Timestamp
import java.util.Calendar

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
      val error = storeError(AppError(code = message.code))
      val occurrence = ErrorOccurrence(error_pk = error.id, date = new Timestamp(message.occurrence.date.getTime))
      ReceivedOk(error, occurrenceRepository.store(occurrence))
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
