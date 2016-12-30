package com.briehman.errorregistry.boundary

import java.time.LocalDateTime

import com.briehman.errorregistry.models.AppError

trait GetErrorSummaryBoundary {
  def getUniqueRecentOccurrenceSummaries(since: LocalDateTime, max: Int): Seq[ErrorSummary]
  def getMostFrequentRecentOccurrencesSummaries(since: LocalDateTime, max: Int): Seq[ErrorSummary]
}

case class ErrorSummary(error: AppError,
                        occurrenceSummary: ErrorOccurrenceSummary)


case class ErrorOccurrenceSummary(error_pk: Int,
                                  firstSeen: LocalDateTime,
                                  lastSeen: LocalDateTime,
                                  totalOccurrences: Int)
