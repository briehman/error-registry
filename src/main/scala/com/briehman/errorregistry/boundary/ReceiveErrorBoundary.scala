package com.briehman.errorregistry.boundary

import com.briehman.errorregistry.message.AppErrorMessage
import com.briehman.errorregistry.models.{AppError, ErrorOccurrence}

trait ReceiveErrorBoundary {
  def receiveError(error: AppErrorMessage): ReceiveErrorResponse
}

sealed trait ReceiveErrorResponse
case class ReceivedOk(error: AppError, occurrence: ErrorOccurrence) extends ReceiveErrorResponse
case object ReceiveFailed extends ReceiveErrorResponse
