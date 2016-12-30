package com.briehman.failureregistry.boundary

import java.time.LocalDateTime

import com.briehman.failureregistry.models.Failure

trait GetFailureSummaryBoundary {
  def getUniqueRecentOccurrenceSummaries(since: LocalDateTime, max: Int): Seq[FailureSummary]
  def getMostFrequentRecentOccurrencesSummaries(since: LocalDateTime, max: Int): Seq[FailureSummary]
}

case class FailureSummary(failure: Failure,
                          occurrenceSummary: FailureOccurrenceSummary)


case class FailureOccurrenceSummary(failure_pk: Int,
                                    firstSeen: LocalDateTime,
                                    lastSeen: LocalDateTime,
                                    totalOccurrences: Int)
