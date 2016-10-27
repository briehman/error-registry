package com.briehman.failureregistry.boundary

import java.time.LocalDateTime

import com.briehman.failureregistry.models.Failure
import com.briehman.failureregistry.repository.FailureOccurrenceSummary

trait ListFailuresBoundary {
  def findFailure(code: String): Option[Failure]
  def getUniqueRecentOccurrenceSummaries(date: LocalDateTime, max: Int): Seq[FailureSummary]
  def getTopRecentOccurrencesSummaries(since: LocalDateTime, max: Int): Seq[FailureSummary]
}

case class FailureSummary(failure: Failure,
                          occurrenceSummary: FailureOccurrenceSummary)
