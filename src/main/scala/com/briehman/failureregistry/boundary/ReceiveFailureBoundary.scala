package com.briehman.failureregistry.boundary

import com.briehman.failureregistry.message.FailureMessage
import com.briehman.failureregistry.models.Failure

trait ReceiveFailureBoundary {
  def receiveFailure(failure: FailureMessage): ReceiveFailureResponse
}

sealed trait ReceiveFailureResponse
case class ReceivedOk(val failure: Failure) extends ReceiveFailureResponse
case object ReceiveFailed extends ReceiveFailureResponse
