package com.briehman.failureregistry.service

import com.briehman.failureregistry.interactor.ReceiveFailureInteractor
import com.briehman.failureregistry.message.FailureMessage

trait ReceiveFailureMessageService {
  val processFailureInteractor: ReceiveFailureInteractor

  def onReceive(failure: FailureMessage) = {
    processFailureInteractor.receiveFailure(failure)
  }
}