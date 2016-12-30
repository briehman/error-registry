package com.briehman.errorregistry.service

import com.briehman.errorregistry.message.AppErrorMessage

trait SendErrorMessageService {
  def sendMessage(error: AppErrorMessage): SendErrorResponse
}

sealed trait SendErrorResponse
case object MessageSent extends SendErrorResponse
case class SendFailed(exception: Exception) extends SendErrorResponse

