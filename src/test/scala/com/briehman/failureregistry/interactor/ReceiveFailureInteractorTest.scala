package com.briehman.failureregistry.interactor

import java.util.Date

import com.briehman.failureregistry.boundary.{ReceiveFailed, ReceivedOk}
import com.briehman.failureregistry.message.{FailureMessage, FailureOccurrenceMessage, RequestInformationMessage}
import com.briehman.failureregistry.models.Failure
import com.briehman.failureregistry.repository.{InMemoryFailureOccurrenceRepository, InMemoryFailureRepository}
import com.briehman.failureregistry.service.NotificationService
import org.mockito.Mockito._
import org.scalatest.Matchers
import org.scalatest.mockito.MockitoSugar

class ReceiveFailureInteractorTest extends org.scalatest.path.FunSpec with Matchers with MockitoSugar {
  private val notificationService = mock[NotificationService]

  describe("ReceiveFailureInteractor") {
    val failureRepository = new InMemoryFailureRepository
    val occurrenceRepository = new InMemoryFailureOccurrenceRepository(failureRepository)
    val interactor = new ReceiveFailureInteractor(failureRepository, occurrenceRepository, notificationService)

    describe("receiving a null failure") {
      val nullFailureResponse = interactor.receiveFailure(null)

      it("responds with a ReceiveFailed") {
        nullFailureResponse shouldBe ReceiveFailed
      }

      it("does not store the failure") {
        failureRepository.find(null) shouldBe None
      }
    }

    describe("receiving a new failure") {
      val msg = buildMessage("newFailure")
      val failure = Failure(msg)
      val response = interactor.receiveFailure(msg)
//      interactor.receiveFailure(msg)

      it("responds with a ReceivedOk") {
        response shouldBe a [ReceivedOk]
      }

      it("persists the new failure") {
        val stored = failureRepository.find(failure.code)
        stored shouldNot be(None)
        stored.get.code shouldBe "newFailure"
      }

      it("notifies about the failure") {
        verify(notificationService, times(1)).notify(failure)
      }

      it("persists the occurrence") {
        val stored = failureRepository.find(failure.code)
        occurrenceRepository.findByFailure(stored.get.code).size shouldBe 1
      }
    }

    describe("receiving an existing failure") {
      val msg = buildMessage("existingFailure")
      val failure = Failure(msg)
      val existingFailure = failureRepository.store(failure)
      val response = interactor.receiveFailure(msg)

      it("responds with a ReceivedOk") {
        response shouldBe a [ReceivedOk]
      }

      it("does not need to persist the existing failure") {
        val stored = failureRepository.find(failure.code)
        stored shouldNot be(None)
        stored.get.id shouldBe existingFailure.id
      }

      it("does not notify about the failure") {
        verify(notificationService, times(0)).notify(failure)
      }
    }
  }

  def buildMessage(code: String): FailureMessage = {
    val request = new RequestInformationMessage(null, null, "GET", null)
    val occurrence = new FailureOccurrenceMessage(new Date, "hostname", "buildNum",
      "branch", "dev", None, None, request)
    new FailureMessage(code, "app", occurrence)
  }
}

