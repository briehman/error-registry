package com.briehman.errorregistry.interactor

import java.sql.Timestamp
import java.time.{LocalDate, LocalDateTime, LocalTime}

import com.briehman.errorregistry.boundary.{ErrorOccurrenceSummary, ErrorSummary}
import com.briehman.errorregistry.models.{AppError, ErrorOccurrence}
import org.scalatest.Matchers
import org.scalatest.path.FunSpec
import com.briehman.errorregistry.repository.{InMemoryErrorOccurrenceRepository, InMemoryErrorRepository}

class GetAppErrorSummaryInteractorTest extends FunSpec with Matchers {
  val errorRepository = new InMemoryErrorRepository
  val occurrenceRepository = new InMemoryErrorOccurrenceRepository(errorRepository)
  val interactor = new GetErrorSummaryInteractor(errorRepository, occurrenceRepository)

  describe("GetErrorSummaryInteractor") {
    describe("finding errors after a timestamp") {
      it("finds those stored immediately following") {
        val dt = LocalDateTime.of(LocalDate.of(2016, 1, 1), LocalTime.of(12, 0, 0, 0))
        val selectError = storeErrorAndOccurrence("find", dt)
        storeErrorAndOccurrence("ignore", dt.minusSeconds(1))
        interactor.getUniqueRecentOccurrenceSummaries(dt.minusSeconds(1), 1) shouldBe
          List(ErrorSummary(selectError, ErrorOccurrenceSummary(selectError.id, dt, dt, 1)))
      }

      it("respects the limit returning the most recent") {
        val startingPoint = LocalDateTime.of(LocalDate.of(2016, 1, 1), LocalTime.of(12, 0, 0, 0))
        storeErrorAndOccurrence("first", startingPoint)
        val secondTime = startingPoint.plusSeconds(1)
        val thirdTime = startingPoint.plusSeconds(2)
        val second = storeErrorAndOccurrence("second", secondTime)
        val third = storeErrorAndOccurrence("third", thirdTime)
        interactor.getUniqueRecentOccurrenceSummaries(startingPoint.minusSeconds(1), 2) shouldBe
          List(ErrorSummary(third, ErrorOccurrenceSummary(third.id, thirdTime, thirdTime, 1)),
            ErrorSummary(second, ErrorOccurrenceSummary(second.id, secondTime, secondTime, 1)))
      }

      it("limits by unique errors and not simply occurrences") {
        val startingPoint = LocalDateTime.of(LocalDate.of(2016, 1, 1), LocalTime.of(12, 0, 0, 0))
        val first = storeErrorAndOccurrence("first", startingPoint)
        val second = storeErrorAndOccurrence("second", startingPoint.plusSeconds(1))
        storeOccurrence(startingPoint.plusSeconds(2), second)
        storeOccurrence(startingPoint.plusSeconds(3), second)
        storeOccurrence(startingPoint.plusSeconds(3), second)
        interactor.getUniqueRecentOccurrenceSummaries(startingPoint.minusSeconds(1), 2) shouldBe
          List(ErrorSummary(second, ErrorOccurrenceSummary(second.id, startingPoint.plusSeconds(1), startingPoint.plusSeconds(3), 4)),
            ErrorSummary(first, ErrorOccurrenceSummary(first.id, startingPoint, startingPoint, 1)))
      }
    }

    describe("listing most frequent errors") {
      it("finds the first occurrence when is the only one") {
        val dt = LocalDateTime.of(LocalDate.of(2016, 1, 1), LocalTime.of(12, 0, 0, 0))
        val selectError = storeErrorAndOccurrence("find", dt)
        interactor.getMostFrequentRecentOccurrencesSummaries(dt.minusSeconds(1), 1) shouldBe
          List(ErrorSummary(selectError, ErrorOccurrenceSummary(selectError.id, dt, dt, 1)))
      }

      it("finds the largest number of occurrences even if they are the oldest") {
        val dt = LocalDateTime.of(LocalDate.of(2016, 1, 1), LocalTime.of(12, 0, 0, 0))
        val selectError = storeErrorAndOccurrence("find", dt)
        for (i <- 1 to 20) {
          storeOccurrence(dt.plusSeconds(i), selectError)
        }
        storeErrorAndOccurrence("skip", dt.plusSeconds(100))
        interactor.getMostFrequentRecentOccurrencesSummaries(dt.minusSeconds(1), 1) shouldBe
          List(ErrorSummary(selectError, ErrorOccurrenceSummary(selectError.id, dt, dt.plusSeconds(20), 21)))
      }
    }
  }

  def storeErrorAndOccurrence(errorCode: String, dt: LocalDateTime): AppError = {
    val selectError = errorRepository.store(new AppError(-1, errorCode))
    storeOccurrence(dt, selectError)
    selectError
  }

  def storeOccurrence(dt: LocalDateTime, selectError: AppError): ErrorOccurrence = {
    occurrenceRepository.store(new ErrorOccurrence(-1, selectError.id, Timestamp.valueOf(dt)))
  }
}
