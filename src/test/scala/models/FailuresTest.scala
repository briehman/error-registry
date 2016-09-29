package models

import java.sql.Date

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers}
import slick.driver.H2Driver.api._

import scala.concurrent.Await
import scala.concurrent.duration._

class FailuresTest extends FlatSpec with Matchers with ScalaFutures with BeforeAndAfter {
  val db = Database.forConfig("db")

  implicit val defaultPatience =
    PatienceConfig(timeout = Span(5, Seconds), interval = Span(500, Millis))

  before {
    val setup = DBIO.seq(FailuresDAO.schema.create)
    Await.ready(db.run(setup), Duration.Inf)
  }

  after {
    val teardown = DBIO.seq(FailuresDAO.schema.drop)
    Await.ready(db.run(teardown), Duration.Inf)
  }

  "Failures" should "be inserted and queried successfully" in {
    val insert = FailuresDAO ++= Seq(
      Failure(1, new Date(System.currentTimeMillis())),
      Failure(2, new Date(System.currentTimeMillis()))
    )
    val f = db.run(insert >> FailuresDAO.result)

    whenReady(f) { results =>
      results shouldBe Seq(Failure(1, null), Failure(2, null))
    }
  }
}
