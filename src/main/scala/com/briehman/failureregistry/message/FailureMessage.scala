package com.briehman.failureregistry.message

import java.net.URI
import java.util.Date

case class RequestInformationMessage(uri: URI,
                                     parameters: String,
                                     `type`: String,
                                     sessionId: String
                             )

case class FailureOccurrenceMessage(date: Date,
                                    hostname: String,
                                    build: String,
                                    branch: String,
                                    environment: String,
                                    issue: Option[String],
                                    user: Option[String],
                                    requestInformation: RequestInformationMessage
                            )

case class FailureMessage(code: String,
                          application: String,
                          occurrence: FailureOccurrenceMessage
                         )