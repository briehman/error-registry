package com.briehman.failureregistry.web

import com.briehman.failureregistry.boundary.{ReceiveFailed, ReceivedOk}
import com.briehman.failureregistry.interactor.ReceiveFailureInteractor
import com.briehman.failureregistry.message.FailureMessage
import com.briehman.failureregistry.repository.{FailureOccurrenceRepository, FailureRepository}
import com.briehman.failureregistry.service.FakeNotificationService
import org.scalatra.ScalatraServlet
import org.scalatra.scalate.ScalateSupport

// JSON-related libraries
import org.json4s.{DefaultFormats, Formats}

// JSON handling support from Scalatra
import org.scalatra.json._

case class DateTest(date: java.util.Date)
class SendFailureResource(implicit val failureRepository: FailureRepository,
                          implicit val occurrenceRepository: FailureOccurrenceRepository)
  extends ScalatraServlet
    with JacksonJsonSupport
    with ScalateSupport {
  override protected implicit def jsonFormats: Formats = DefaultFormats + DateSerializer + CustomTimestampSerializer + UriSerializer

  post("/failure") {
    val failureMessage = parsedBody.extract[FailureMessage]
    val messageTranslator = (msg: FailureMessage) => null
    val interactor = new ReceiveFailureInteractor(failureRepository, occurrenceRepository, new FakeNotificationService)
    val response = interactor.receiveFailure(failureMessage)

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

  post("/date") {
    val dateTest = parsedBody.extract[DateTest]
    <html>
      <body>
        <h1>You posted a failure for date {dateTest.date}</h1>
      </body>
    </html>
  }

  get("/list") {
    findTemplate(requestPath) map { path =>
      contentType = "text/html"
      val failures = failureRepository.listCodes map { code =>
        code -> occurrenceRepository.findByFailure(code).map(_.toString)
      }
      layoutTemplate(path, "failures" -> failures)
    } orElse serveStaticResource() getOrElse resourceNotFound()
  }

}
