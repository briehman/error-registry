
import java.time.LocalDateTime

import com.briehman.failureregistry.boundary.ListFailuresBoundary
import com.briehman.failureregistry.repository.{FailureOccurrenceRepository, FailureRepository}
import com.briehman.failureregistry.web.{CustomTimestampSerializer, DateSerializer, UriSerializer}
import org.scalatra.ScalatraServlet
import org.scalatra.scalate.ScalateSupport

// JSON-related libraries
import org.json4s.{DefaultFormats, Formats}

// JSON handling support from Scalatra
import org.scalatra.json._

case class DateTest(date: java.util.Date)

class HomePageServlet(listFailures: ListFailuresBoundary)
  extends ScalatraServlet
    with JacksonJsonSupport
    with ScalateSupport {
  override protected implicit def jsonFormats: Formats = DefaultFormats + DateSerializer + CustomTimestampSerializer + UriSerializer

  get("/") {
    contentType = "text/html"
    val recentFailures = listFailures.getUniqueRecentOccurrenceSummaries(LocalDateTime.now().minusMinutes(1), 5)
    scaml("/WEB-INF/views/home.scaml", "recentFailures" -> recentFailures)
  }

}
