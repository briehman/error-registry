package com.briehman.failureregistry.web.api

import com.briehman.failureregistry.boundary.{ReceiveFailed, ReceivedOk}
import com.briehman.failureregistry.interactor.ReceiveFailureInteractor
import com.briehman.failureregistry.message.FailureMessage
import com.briehman.failureregistry.web.{UriSerializer, CustomTimestampSerializer, DateSerializer}
import org.scalatra.ScalatraServlet
import org.scalatra.scalate.ScalateSupport

// JSON-related libraries
import org.json4s.{DefaultFormats, Formats}

// JSON handling support from Scalatra
import org.scalatra.json._

class SendFailureResource(receiveFailureInteractor: ReceiveFailureInteractor)
  extends ScalatraServlet
    with JacksonJsonSupport
    with ScalateSupport {
  override protected implicit def jsonFormats: Formats = DefaultFormats + DateSerializer + CustomTimestampSerializer + UriSerializer

  post("/") {
    val failureMessage = parsedBody.extract[FailureMessage]
    val response = receiveFailureInteractor.receiveFailure(failureMessage)

    response match {
      case ReceivedOk(f, o) =>
        <html>
          <body>
            <h1>You posted a failure for {f.code} on {o.date}</h1>
          </body>
        </html>
      case ReceiveFailed =>
        <html>
          <body>
            <h1>Failure receipt failed</h1>
          </body>
        </html>
    }
  }

//  get("/errors") {
//    findTemplate(requestPath) map { path =>
//      contentType = "text/html"
//      val failures = failureRepository.listCodes map { code =>
//        code -> occurrenceRepository.findByFailure(code).map(_.toString)
//      }
//      layoutTemplate(path, "failures" -> failures)
//    } orElse serveStaticResource() getOrElse resourceNotFound()
//  }

}
