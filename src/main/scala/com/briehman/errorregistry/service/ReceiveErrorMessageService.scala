package com.briehman.errorregistry.service

import com.briehman.errorregistry.interactor.ReceiveErrorInteractor
import com.briehman.errorregistry.message.AppErrorMessage

trait ReceiveErrorMessageService {
  val interactor: ReceiveErrorInteractor

  def onReceive(error: AppErrorMessage) = {
    interactor.receiveError(error)
  }
}