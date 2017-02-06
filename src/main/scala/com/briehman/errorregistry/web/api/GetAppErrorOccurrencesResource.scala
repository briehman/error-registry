package com.briehman.errorregistry.web.api

import com.briehman.errorregistry.interactor.GetAppErrorOccurrenceInteractor
import com.briehman.errorregistry.web.AppErrorJsonFormats
import org.json4s.Extraction
import org.scalatra.ScalatraServlet
import org.scalatra.scalate.ScalateSupport

class GetAppErrorOccurrencesResource(interactor: GetAppErrorOccurrenceInteractor)
  extends ScalatraServlet
    with AppErrorJsonFormats
    with ScalateSupport {
  get("/:appErrorId") {
    val appErrorId = params("appErrorId").toInt
    val startAt = List(0, params.getOrElse("start", "0").toInt).max
    val maxResults = List(100, params.getOrElse("length", "10").toInt).min
    val occurrences = interactor.getOccurrences(appErrorId, startAt, maxResults)
      .map(o => o.transformForApi)

    val data = Map("data" -> occurrences,
      "start" -> startAt,
      "recordsTotal" -> interactor.countTotalOccurrences(appErrorId),
      "recordsFiltered" -> occurrences.size)

    Extraction.decompose(data)
  }
}
