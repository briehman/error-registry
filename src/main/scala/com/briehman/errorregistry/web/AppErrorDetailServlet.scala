package com.briehman.errorregistry.web

import com.briehman.errorregistry.boundary.GetAppErrorDetailBoundary
import org.scalatra.ScalatraServlet
import org.scalatra.scalate.ScalateSupport

// JSON-related libraries
import org.json4s.{DefaultFormats, Formats}

// JSON handling support from Scalatra
import org.scalatra.json._

class AppErrorDetailServlet(appErrorDetailBoundary: GetAppErrorDetailBoundary)
  extends ScalatraServlet
    with JacksonJsonSupport
    with ScalateSupport {
  override protected implicit def jsonFormats: Formats = DefaultFormats + DateSerializer + CustomTimestampSerializer + UriSerializer

  get("/:error") {
    contentType = "text/html"

    val requestedError = params("error")
    val details = try {
      val errorId = requestedError.toInt
      appErrorDetailBoundary.getDetails(errorId)
    } catch {
      case _: NumberFormatException => appErrorDetailBoundary.getDetails(requestedError)
    }


    scaml("/WEB-INF/views/appErrorDetail.scaml",
      "requestedError" -> requestedError,
      "details" -> details
    )
  }

}
