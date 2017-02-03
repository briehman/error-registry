package com.briehman.errorregistry.web.api

import com.briehman.errorregistry.interactor.GetAppErrorOccurrenceInteractor
import com.briehman.errorregistry.web.{CustomTimestampSerializer, DateSerializer, UriSerializer}
import org.scalatra.ScalatraServlet
import org.scalatra.scalate.ScalateSupport

// JSON-related libraries
import org.json4s.{DefaultFormats, Formats}

// JSON handling support from Scalatra
import org.scalatra.json._

class GetAppErrorOccurrencesResource(interactor: GetAppErrorOccurrenceInteractor)
  extends ScalatraServlet
    with JacksonJsonSupport
    with ScalateSupport {
  override protected implicit def jsonFormats: Formats = DefaultFormats + DateSerializer + CustomTimestampSerializer + UriSerializer

  get("/:appErrorId") {
    val appErrorId = params("appErrorId").toInt
    val startAt = List(0, params.getOrElse("start", "0").toInt).max
    val maxResults = List(100, params.getOrElse("length", "10").toInt).min
    val occurrences = interactor.getOccurrences(appErrorId, startAt, maxResults)
    Map("data" -> occurrences,
      "start" -> startAt,
      "recordsTotal" -> 100,
      "recordsFiltered" -> occurrences.size)
  }
}
