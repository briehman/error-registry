package com.briehman.failureregistry.models

import java.sql.Timestamp
import java.util.Calendar

import com.briehman.failureregistry.message.FailureMessage

case class Failure(
                  id: Int,
                  code: String
                  )


object Failure {
  def apply(message: FailureMessage) = new Failure(-1, message.code)
}

case class FailureOccurrence(
                            id: Int,
                            failure_pk: Int,
                            date: Timestamp
                            )

object FailureOccurrence {
  def apply(message: FailureMessage, failure: Failure) = {
    new FailureOccurrence(-1, failure.id, new Timestamp(Calendar.getInstance().getTime.getTime))
  }
}