package com.briehman.errorregistry.interactor

import java.sql.Timestamp
import java.time.{LocalDate, LocalDateTime, LocalTime}

import com.briehman.errorregistry.boundary.{ErrorOccurrenceSummary, ErrorSummary}
import com.briehman.errorregistry.models.{AppError, ErrorOccurrence}
import com.briehman.errorregistry.repository.memory.{InMemoryErrorOccurrenceRepository, InMemoryErrorRepository}
import org.scalatest.Matchers
import org.scalatest.path.FunSpec

class GetErrorSummaryInteractorTest extends FunSpec with Matchers {
  val errorRepository = new InMemoryErrorRepository
  val occurrenceRepository = new InMemoryErrorOccurrenceRepository(errorRepository)
  val interactor = new GetErrorSummaryInteractor(occurrenceRepository)

  describe("GetErrorSummaryInteractor") {
    describe("listing recent errors after a timestamp") {
      it("finds those stored immediately following") {
        val dt = LocalDateTime.of(LocalDate.of(2016, 1, 1), LocalTime.of(12, 0, 0, 0))
        val selectError = storeErrorAndOccurrence("find", dt)
        storeErrorAndOccurrence("ignore", dt.minusSeconds(1))
        interactor.listRecent(dt.minusSeconds(1), 1) shouldBe
          List(ErrorSummary(selectError.id, "find", ErrorOccurrenceSummary(selectError.id, dt, dt, 1, 1)))
      }

      it("respects the limit returning the most recent") {
        val startingPoint = LocalDateTime.of(LocalDate.of(2016, 1, 1), LocalTime.of(12, 0, 0, 0))
        storeErrorAndOccurrence("first", startingPoint)
        val secondTime = startingPoint.plusSeconds(1)
        val thirdTime = startingPoint.plusSeconds(2)
        val second = storeErrorAndOccurrence("second", secondTime)
        val third = storeErrorAndOccurrence("third", thirdTime)
        interactor.listRecent(startingPoint.minusSeconds(1), 2) shouldBe
          List(ErrorSummary(third.id, "third", ErrorOccurrenceSummary(third.id, thirdTime, thirdTime, 1, 1)),
            ErrorSummary(second.id, "second", ErrorOccurrenceSummary(second.id, secondTime, secondTime, 1, 1)))
      }

      it("limits by unique errors and not simply occurrences") {
        val startingPoint = LocalDateTime.of(LocalDate.of(2016, 1, 1), LocalTime.of(12, 0, 0, 0))
        val first = storeErrorAndOccurrence("first", startingPoint)
        val second = storeErrorAndOccurrence("second", startingPoint.plusSeconds(1))
        storeOccurrence(startingPoint.plusSeconds(2), second)
        storeOccurrence(startingPoint.plusSeconds(3), second)
        storeOccurrence(startingPoint.plusSeconds(3), second)
        interactor.listRecent(startingPoint.minusSeconds(1), 2) shouldBe
          List(ErrorSummary(second.id, "second", ErrorOccurrenceSummary(second.id, startingPoint.plusSeconds(1), startingPoint.plusSeconds(3), 4, 4)),
            ErrorSummary(first.id, "first", ErrorOccurrenceSummary(first.id, startingPoint, startingPoint, 1, 1)))
      }
    }

    describe("listing most frequent errors") {
      it("finds the first occurrence when is the only one") {
        val dt = LocalDateTime.of(LocalDate.of(2016, 1, 1), LocalTime.of(12, 0, 0, 0))
        val selectError = storeErrorAndOccurrence("find", dt)
        interactor.listMostFrequent(dt.minusSeconds(1), 1) shouldBe
          List(ErrorSummary(selectError.id, "find", ErrorOccurrenceSummary(selectError.id, dt, dt, 1, 1)))
      }

      it("finds the largest number of occurrences even if they are the oldest") {
        val dt = LocalDateTime.of(LocalDate.of(2016, 1, 1), LocalTime.of(12, 0, 0, 0))
        val selectError = storeErrorAndOccurrence("find", dt)
        for (i <- 1 to 20) {
          storeOccurrence(dt.plusSeconds(i), selectError)
        }
        storeErrorAndOccurrence("skip", dt.plusSeconds(100))
        interactor.listMostFrequent(dt.minusSeconds(1), 1) shouldBe
          List(ErrorSummary(selectError.id, "find", ErrorOccurrenceSummary(selectError.id, dt, dt.plusSeconds(20), 21, 21)))
      }
    }

    describe("listing new errors") {
      it("respects the limit returning the most recent") {
        val startingPoint = LocalDateTime.of(LocalDate.of(2016, 1, 1), LocalTime.of(12, 0, 0, 0))
        storeErrorAndOccurrence("first", startingPoint)
        val secondTime = startingPoint.plusSeconds(1)
        val thirdTime = startingPoint.plusSeconds(2)
        val second = storeErrorAndOccurrence("second", secondTime)
        val third = storeErrorAndOccurrence("third", thirdTime)
        interactor.listNew(startingPoint.minusSeconds(1), 2) shouldBe
          List(ErrorSummary(third.id, "third", ErrorOccurrenceSummary(third.id, thirdTime, thirdTime, 1, 1)),
            ErrorSummary(second.id, "second", ErrorOccurrenceSummary(second.id, secondTime, secondTime, 1, 1)))
      }

      it("ignores recent occurrences of old errors") {
        val startingPoint = LocalDateTime.of(LocalDate.of(2016, 1, 1), LocalTime.of(12, 0, 0, 0))
        val error = storeErrorAndOccurrence("first", startingPoint)
        storeOccurrence(startingPoint.plusSeconds(1), error)
        storeOccurrence(startingPoint.plusSeconds(2), error)
        interactor.listNew(startingPoint.plusSeconds(1), 2).isEmpty shouldBe true
      }
    }
  }

  def storeErrorAndOccurrence(errorCode: String, dt: LocalDateTime): AppError = {
    val selectError = errorRepository.store(new AppError(code = errorCode, error = ""))
    storeOccurrence(dt, selectError)
    selectError
  }

  def storeOccurrence(dt: LocalDateTime, selectError: AppError): ErrorOccurrence = {
    occurrenceRepository.store(ErrorOccurrence(error_pk = selectError.id, date = Timestamp.valueOf(dt)))
  }
}
