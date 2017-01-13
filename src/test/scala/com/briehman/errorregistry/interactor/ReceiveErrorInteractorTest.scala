package com.briehman.errorregistry.interactor

import java.util.Date

import com.briehman.errorregistry.boundary.{ReceiveFailed, ReceivedOk}
import com.briehman.errorregistry.message.{AppErrorMessage, ErrorOccurrenceMessage, RequestInformationMessage}
import com.briehman.errorregistry.models.AppError
import com.briehman.errorregistry.repository.{InMemoryErrorOccurrenceRepository, InMemoryErrorRepository}
import com.briehman.errorregistry.service.NotificationService
import org.mockito.Mockito._
import org.scalatest.Matchers
import org.scalatest.mockito.MockitoSugar

class ReceiveErrorInteractorTest extends org.scalatest.path.FunSpec with Matchers with MockitoSugar {
  private val notificationService = mock[NotificationService]

  describe("ReceiveErrorInteractor") {
    val errorRepository = new InMemoryErrorRepository
    val occurrenceRepository = new InMemoryErrorOccurrenceRepository(errorRepository)
    val interactor = new ReceiveErrorInteractor(errorRepository, occurrenceRepository, notificationService)

    describe("receiving a null errorMessage") {
      val nullFailureResponse = interactor.receiveError(null)

      it("responds with a ReceiveFailed") {
        nullFailureResponse shouldBe ReceiveFailed
      }

      it("does not store the errorMessage") {
        errorRepository.find(null) shouldBe None
      }
    }

    describe("receiving a new errorMessage") {
      val msg = buildMessage("newError")
      val error = AppError(code = msg.code)
      val response = interactor.receiveError(msg)

      it("responds with a ReceivedOk") {
        response shouldBe a [ReceivedOk]
      }

      it("persists the new errorMessage") {
        val stored = errorRepository.find("newError")
        stored shouldNot be(None)
        stored.get.code shouldBe "newError"
      }

      it("notifies about the errorMessage") {
        verify(notificationService, times(1)).notify(error)
      }

      it("stores the message date") {
        response.asInstanceOf[ReceivedOk].occurrence.date.toInstant shouldBe msg.occurrence.date.toInstant
      }

      it("persists the occurrence by the given code") {
        val stored = errorRepository.find(error.code)
        occurrenceRepository.findByCode(stored.get.code).size shouldBe 1
      }
    }

    describe("receiving a previously stored errorMessage") {
      val msg = buildMessage("existingError")
      val error = AppError(code = msg.code)
      val existingError = errorRepository.store(error)
      val response = interactor.receiveError(msg)

      it("responds with a ReceivedOk") {
        response shouldBe a [ReceivedOk]
      }

      it("does not persist the existing errorMessage") {
        val stored = errorRepository.find(error.code)
        stored.get.id shouldBe existingError.id
      }

      it("does not notify about the errorMessage") {
        verify(notificationService, times(0)).notify(error)
      }

      it("persists the occurrence") {
        val stored = errorRepository.find(error.code)
        occurrenceRepository.findByCode(stored.get.code).size shouldBe 1
      }
    }
  }

  def buildMessage(code: String): AppErrorMessage = {
    val request = new RequestInformationMessage(null, null, "GET", null)
    val occurrence = new ErrorOccurrenceMessage(new Date, "hostname", "buildNum",
      "branch", "dev", None, None, request)
    new AppErrorMessage(code, "app", occurrence)
  }
}

