package com.briehman.errorregistry.web


import java.time.LocalDateTime

import com.briehman.errorregistry.boundary.GetErrorSummaryBoundary
import org.scalatra.ScalatraServlet
import org.scalatra.scalate.ScalateSupport

// JSON-related libraries
import org.json4s.{DefaultFormats, Formats}

// JSON handling support from Scalatra
import org.scalatra.json._

class HomePageServlet(errorSummaryBoundary: GetErrorSummaryBoundary)
  extends ScalatraServlet
    with JacksonJsonSupport
    with ScalateSupport {
  override protected implicit def jsonFormats: Formats = DefaultFormats + DateSerializer + CustomTimestampSerializer + UriSerializer

  get("/") {
    contentType = "text/html"
    val recentErrors = errorSummaryBoundary.getUniqueRecentOccurrenceSummaries(LocalDateTime.now().minusMinutes(1), 5)
    val mostFrequentErrors = errorSummaryBoundary.getMostFrequentRecentOccurrencesSummaries(LocalDateTime.now().minusMinutes(1), 5)

    scaml("/WEB-INF/views/home.scaml",
      "recentErrors" -> recentErrors,
      "mostFrequentErrors" -> mostFrequentErrors)
  }

}
