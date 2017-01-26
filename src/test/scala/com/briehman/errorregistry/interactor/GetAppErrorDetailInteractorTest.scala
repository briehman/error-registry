package com.briehman.errorregistry.interactor

import java.net.URI
import java.sql.Timestamp
import java.time._

import com.briehman.errorregistry.boundary._
import com.briehman.errorregistry.message.{AppErrorMessage, ErrorOccurrenceMessage, RequestInformationMessage}
import com.briehman.errorregistry.models._
import com.briehman.errorregistry.repository.memory._
import org.scalatest.Matchers
import org.scalatest.path.FunSpec

class GetAppErrorDetailInteractorTest extends FunSpec with Matchers {
  val errorRepository = new InMemoryErrorRepository
  val occurrenceRepository = new InMemoryErrorOccurrenceRepository(errorRepository)
  val interactor = new GetAppErrorDetailInteractor(errorRepository, occurrenceRepository)
  describe("GetAppErrorDetailInteractorTest") {
    describe("getting details by id") {
      it("finds None for unknown id") {
        interactor.getDetails(1) shouldBe None
      }

      describe("with an existing error") {
        val occurrenceMsg = ErrorOccurrenceMessage(hostname = "localhost", build = "test",
          branch = "test",
          requestInformation = RequestInformationMessage("http://localhost", "GET"))
        val error = errorRepository.store(AppError(AppErrorMessage("code", "fundbutter",
          occurrenceMsg,
          "errorString")))

        it("finds the basic details") {
          val occurrence = occurrenceRepository.store(ErrorOccurrence(message = occurrenceMsg, error = error))
          val response = interactor.getDetails(error.id)
          response shouldBe defined
          response.get.errorId shouldBe error.id
          response.get.code shouldBe "code"
          response.get.message shouldBe "errorString"
        }

        it("finds the stats") {
          val occurrence = occurrenceRepository.store(ErrorOccurrence(message = occurrenceMsg, error = error))
          val response = interactor.getDetails(error.id)
          response shouldBe defined
          response.get.summary.firstSeen shouldBe occurrence.date.toLocalDateTime
          response.get.summary.lastSeen shouldBe occurrence.date.toLocalDateTime
        }
      }
    }
  }
}