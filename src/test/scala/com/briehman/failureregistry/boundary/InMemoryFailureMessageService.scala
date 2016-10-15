package com.briehman.failureregistry.boundary

import com.briehman.failureregistry.interactor.ReceiveFailureInteractor
import com.briehman.failureregistry.message.FailureMessage
import com.briehman.failureregistry.service.{MessageSent, SendFailureResponse, SendFailureMessageService, ReceiveFailureMessageService}

class InMemoryFailureMessageService(val processFailureInteractor: ReceiveFailureInteractor)
  extends SendFailureMessageService
  with ReceiveFailureMessageService {
  override def sendMessage(failure: FailureMessage): SendFailureResponse = {
    onReceive(failure)
    MessageSent
  }

  override def stop(): Unit = {}

  override def start(): Unit = {}
}
