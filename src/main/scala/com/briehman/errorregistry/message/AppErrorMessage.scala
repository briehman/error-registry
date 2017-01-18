package com.briehman.errorregistry.message

import java.net.URI
import java.util.Date

import com.briehman.errorregistry.models.AppError

case class AppErrorMessage(code: String,
                           application: String,
                           occurrence: ErrorOccurrenceMessage,
                           errorString: String
                          )

case class ErrorOccurrenceMessage(date: Date = new Date(),
                                  hostname: String,
                                  build: String,
                                  branch: String,
                                  environment: String,
                                  issue: Option[String],
                                  user: Option[String],
                                  requestInformation: RequestInformationMessage
                                 )

case class RequestInformationMessage(uri: URI,
                                     methodType: String,
                                     parameters: Option[String],
                                     sessionId: Option[String])