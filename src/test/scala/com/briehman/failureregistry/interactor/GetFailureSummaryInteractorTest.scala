package com.briehman.failureregistry.interactor

import java.sql.Timestamp
import java.time.{LocalTime, LocalDate, LocalDateTime}

import com.briehman.failureregistry.boundary.{FailureOccurrenceSummary, FailureSummary}
import com.briehman.failureregistry.models.{FailureOccurrence, Failure}
import org.scalatest.Matchers
import org.scalatest.path.FunSpec
import com.briehman.failureregistry.repository.{FailureOccurrenceSummary, InMemoryFailureOccurrenceRepository, InMemoryFailureRepository}

class GetFailureSummaryInteractorTest extends FunSpec with Matchers {
  val failureRepository = new InMemoryFailureRepository
  val occurrenceRepository = new InMemoryFailureOccurrenceRepository(failureRepository)
  val interactor = new GetFailureSummaryInteractor(failureRepository, occurrenceRepository)

  describe("GetFailureSummaryInteractor") {
    it("retrieves stored failures") {
      val failure = failureRepository.store(new Failure(-2, "existing"))
      interactor.findFailure("existing") shouldBe Some(failure)
    }

    it("returns None for unknown failures") {
      interactor.findFailure("unknown") shouldBe None
    }

    describe("finding failures after a timestamp") {
      it("finds those stored immediately following") {
        val dt = LocalDateTime.of(LocalDate.of(2016, 1, 1), LocalTime.of(12, 0, 0, 0))
        val selectFailure = storeFailureAndOccurrence("find", dt)
        storeFailureAndOccurrence("ignore", dt.minusSeconds(1))
        interactor.getUniqueRecentOccurrenceSummaries(dt.minusSeconds(1), 1) shouldBe
          List(FailureSummary(selectFailure, FailureOccurrenceSummary(selectFailure.id, dt, dt, 1)))
      }

      it("respects the limit returning the most recent") {
        val startingPoint = LocalDateTime.of(LocalDate.of(2016, 1, 1), LocalTime.of(12, 0, 0, 0))
        storeFailureAndOccurrence("first", startingPoint)
        val secondTime = startingPoint.plusSeconds(1)
        val thirdTime = startingPoint.plusSeconds(2)
        val second = storeFailureAndOccurrence("second", secondTime)
        val third = storeFailureAndOccurrence("third", thirdTime)
        interactor.getUniqueRecentOccurrenceSummaries(startingPoint.minusSeconds(1), 2) shouldBe
          List(FailureSummary(second, FailureOccurrenceSummary(second.id, secondTime, secondTime, 1)),
            FailureSummary(third, FailureOccurrenceSummary(third.id, thirdTime, thirdTime, 1)))
      }

      it("limits by unique failures and not simply occurrences") {
        val startingPoint = LocalDateTime.of(LocalDate.of(2016, 1, 1), LocalTime.of(12, 0, 0, 0))
        val first = storeFailureAndOccurrence("first", startingPoint)
        val second = storeFailureAndOccurrence("second", startingPoint.plusSeconds(1))
        storeOccurrence(startingPoint.plusSeconds(2), second)
        storeOccurrence(startingPoint.plusSeconds(3), second)
        storeOccurrence(startingPoint.plusSeconds(3), second)
        interactor.getUniqueRecentOccurrenceSummaries(startingPoint.minusSeconds(1), 2) shouldBe
          List(FailureSummary(first, FailureOccurrenceSummary(first.id, startingPoint, startingPoint, 1)),
            FailureSummary(second, FailureOccurrenceSummary(second.id, startingPoint.plusSeconds(1), startingPoint.plusSeconds(3), 4)))
      }
    }

    describe("listing top failures") {
      it("finds the first occurrence when is the only one") {
        val dt = LocalDateTime.of(LocalDate.of(2016, 1, 1), LocalTime.of(12, 0, 0, 0))
        val selectFailure = storeFailureAndOccurrence("find", dt)
        interactor.getTopRecentOccurrencesSummaries(dt.minusSeconds(1), 1) shouldBe
          List(FailureSummary(selectFailure, FailureOccurrenceSummary(selectFailure.id, dt, dt, 1)))
      }

      it("finds the largest number of occurrences even if they are the oldest") {
        val dt = LocalDateTime.of(LocalDate.of(2016, 1, 1), LocalTime.of(12, 0, 0, 0))
        val selectFailure = storeFailureAndOccurrence("find", dt)
        for (i <- 1 to 20) {
          storeOccurrence(dt.plusSeconds(i), selectFailure)
        }
        storeFailureAndOccurrence("skip", dt.plusSeconds(100))
        interactor.getTopRecentOccurrencesSummaries(dt.minusSeconds(1), 1) shouldBe
          List(FailureSummary(selectFailure, FailureOccurrenceSummary(selectFailure.id, dt, dt.plusSeconds(20), 21)))
      }
    }
  }

  def storeFailureAndOccurrence(failureCode: String, dt: LocalDateTime): Failure = {
    val selectFailure = failureRepository.store(new Failure(-1, failureCode))
    storeOccurrence(dt, selectFailure)
    selectFailure
  }

  def storeOccurrence(dt: LocalDateTime, selectFailure: Failure): FailureOccurrence = {
    occurrenceRepository.store(new FailureOccurrence(-1, selectFailure.id, Timestamp.valueOf(dt)))
  }
}
