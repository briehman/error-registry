package com.briehman.failureregistry.web


import java.time.LocalDateTime

import com.briehman.failureregistry.boundary.GetFailureSummaryBoundary
import org.scalatra.ScalatraServlet
import org.scalatra.scalate.ScalateSupport

// JSON-related libraries
import org.json4s.{DefaultFormats, Formats}

// JSON handling support from Scalatra
import org.scalatra.json._

class HomePageServlet(failureSummaryBoundary: GetFailureSummaryBoundary)
  extends ScalatraServlet
    with JacksonJsonSupport
    with ScalateSupport {
  override protected implicit def jsonFormats: Formats = DefaultFormats + DateSerializer + CustomTimestampSerializer + UriSerializer

  get("/") {
    contentType = "text/html"
    val recentFailures = failureSummaryBoundary.getUniqueRecentOccurrenceSummaries(LocalDateTime.now().minusMinutes(1), 5)
    scaml("/WEB-INF/views/home.scaml", "recentFailures" -> recentFailures)
  }

}
