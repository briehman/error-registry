package com.briehman.errorregistry.message

import java.net.URI
import java.util.Date

case class RequestInformationMessage(uri: URI,
                                     parameters: String,
                                     `type`: String,
                                     sessionId: String
                             )

case class ErrorOccurrenceMessage(date: Date,
                                  hostname: String,
                                  build: String,
                                  branch: String,
                                  environment: String,
                                  issue: Option[String],
                                  user: Option[String],
                                  requestInformation: RequestInformationMessage
                            )

case class AppErrorMessage(code: String,
                           application: String,
                           occurrence: ErrorOccurrenceMessage
                         )