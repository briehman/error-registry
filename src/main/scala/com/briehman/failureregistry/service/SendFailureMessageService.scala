package com.briehman.failureregistry.service

import com.briehman.failureregistry.message.FailureMessage

trait SendFailureMessageService {
  def sendMessage(failure: FailureMessage): SendFailureResponse
}

sealed trait SendFailureResponse
case object MessageSent extends SendFailureResponse
case class SendFailed(exception: Exception) extends SendFailureResponse

