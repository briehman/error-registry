package com.briehman.errorregistry.models

import java.sql.Timestamp
import java.util.Calendar

import com.briehman.errorregistry.message.AppErrorMessage

case class AppError(
                  id: Int,
                  code: String
                  )


object AppError {
  def apply(message: AppErrorMessage) = new AppError(-1, message.code)
}

case class ErrorOccurrence(
                            id: Int,
                            error_pk: Int,
                            date: Timestamp
                            )

object ErrorOccurrence {
  def apply(message: AppErrorMessage, error: AppError): ErrorOccurrence = {
    new ErrorOccurrence(-1, error.id, new Timestamp(Calendar.getInstance().getTime.getTime))
  }
}