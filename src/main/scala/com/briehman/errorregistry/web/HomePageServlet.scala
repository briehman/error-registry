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
    with AppErrorJsonFormats
    with ScalateSupport {
  get("/") {
    contentType = "text/html"
    // TODO go back to 1 week
    val newErrors = errorSummaryBoundary.listNew(LocalDateTime.now().minusWeeks(2), 5)
    val recentErrors = errorSummaryBoundary.listRecent(LocalDateTime.now().minusWeeks(2), 5)
    val mostFrequentWeeklyErrors = errorSummaryBoundary.listMostFrequent(LocalDateTime.now().minusWeeks(2), 10)
    val mostFrequentMonthlyErrors = errorSummaryBoundary.listMostFrequent(LocalDateTime.now().minusMonths(1), 10)

    scaml("/WEB-INF/views/home.scaml",
      "newErrors" -> newErrors,
      "recentErrors" -> recentErrors,
      "mostFrequentWeeklyErrors" -> mostFrequentWeeklyErrors,
      "mostFrequentMonthlyErrors" -> mostFrequentMonthlyErrors
    )
  }

}
