package com.briehman.failureregistry.interactor

import com.briehman.failureregistry.models.Failure
import org.scalatest.{Matchers, FunSpec}
import com.briehman.failureregistry.repository.InMemoryFailureRepository

class ListFailuresInteractorTest extends FunSpec with Matchers {
  val failureRepository = new InMemoryFailureRepository
  val interactor = new ListFailuresInteractor(failureRepository)

  describe("ListFailuresInteractor") {
    it("retrieves stored failures") {

      val failure = failureRepository.store(new Failure(-2, "existing"))
      interactor.findFailure("existing") shouldBe Some(failure)
    }

    it("returns None for unknown failures") {
      interactor.findFailure("unknown") shouldBe None
    }
  }
}
