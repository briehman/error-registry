package com.briehman.failureregistry.web

import com.briehman.failureregistry.boundary.{ReceiveFailed, ReceivedOk}
import com.briehman.failureregistry.interactor.ReceiveFailureInteractor
import com.briehman.failureregistry.message.FailureMessage
import com.briehman.failureregistry.repository.FailureRepository
import org.scalatra.ScalatraServlet

// JSON-related libraries
import org.json4s.{DefaultFormats, Formats}

// JSON handling support from Scalatra
import org.scalatra.json._

case class DateTest(date: java.util.Date)
class SendFailureResource(implicit val failureRepository: FailureRepository) extends ScalatraServlet with JacksonJsonSupport {
  override protected implicit def jsonFormats: Formats = DefaultFormats + DateSerializer + CustomTimestampSerializer + UriSerializer

  post("/failure") {
    val failureMessage = parsedBody.extract[FailureMessage]
    val messageTranslator = (msg: FailureMessage) => null
    val interactor = new ReceiveFailureInteractor(failureRepository, null, null)
    val response = interactor.receiveFailure(failureMessage)

    response match {
      case ReceivedOk(f) =>
        <html>
          <body>
            <h1>You posted a failure for date {f.code}</h1>
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
    <html>
      <body>
        <h1>Listing codes</h1>
        <p>{failureRepository.listCodes}</p>
      </body>
    </html>
  }

}
