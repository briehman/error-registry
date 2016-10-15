package com.briehman.failureregistry.boundary

import com.briehman.failureregistry.message.FailureMessage
import com.briehman.failureregistry.models.{FailureOccurrence, Failure}

trait ReceiveFailureBoundary {
  def receiveFailure(failure: FailureMessage): ReceiveFailureResponse
}

sealed trait ReceiveFailureResponse
case class ReceivedOk(val failure: Failure, val occurrence: FailureOccurrence) extends ReceiveFailureResponse
case object ReceiveFailed extends ReceiveFailureResponse
