package com.briehman.errorregistry.web.api

import com.briehman.errorregistry.boundary.{ReceiveFailed, ReceivedOk}
import com.briehman.errorregistry.interactor.ReceiveErrorInteractor
import com.briehman.errorregistry.message.AppErrorMessage
import com.briehman.errorregistry.web.{AppErrorJsonFormats, CustomTimestampSerializer, DateSerializer, UriSerializer}
import org.scalatra.ScalatraServlet
import org.scalatra.scalate.ScalateSupport

// JSON-related libraries
import org.json4s.{DefaultFormats, Formats}

// JSON handling support from Scalatra
import org.scalatra.json._

class ErrorApiResource(receiveErrorInteractor: ReceiveErrorInteractor)
  extends ScalatraServlet
    with AppErrorJsonFormats
    with ScalateSupport {
  post("/") {
    val errorMessage = parsedBody.extract[AppErrorMessage]
    val response = receiveErrorInteractor.receiveError(errorMessage)

    response match {
      case ReceivedOk(e, o) =>
        <html>
          <body>
            <h1>You posted a failure for {e.code} on {o.date}</h1>
          </body>
        </html>
      case ReceiveFailed =>
        <html>
          <body>
            <h1>Error receipt failed</h1>
          </body>
        </html>
    }
  }
}
