package com.briehman.errorregistry.web

import com.briehman.errorregistry.boundary.GetAppErrorDetailBoundary
import org.scalatra.ScalatraServlet
import org.scalatra.scalate.ScalateSupport

class AppErrorDetailServlet(appErrorDetailBoundary: GetAppErrorDetailBoundary)
  extends ScalatraServlet
    with AppErrorJsonFormats
    with ScalateSupport {
  get("/:error") {
    contentType = "text/html"

    val requestedError = params("error")

    val idOrCode = if (requestedError.forall(_.isDigit)) {
      Left(requestedError.toInt)
    } else {
      Right(requestedError)
    }

    val details = idOrCode match {
      case Left(id) =>
        appErrorDetailBoundary.getDetails(id)
          .orElse(appErrorDetailBoundary.getDetails(requestedError))
      case Right(code) => appErrorDetailBoundary.getDetails(code)
    }

    scaml("/WEB-INF/views/appErrorDetail.scaml",
      "requestedError" -> requestedError,
      "details" -> details
    )
  }

}
