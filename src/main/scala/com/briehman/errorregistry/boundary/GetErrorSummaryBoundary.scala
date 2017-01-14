package com.briehman.errorregistry.boundary

import java.time.LocalDateTime

import com.briehman.errorregistry.models.AppError

trait GetErrorSummaryBoundary {
  def listNew(since: LocalDateTime, max: Int): Seq[ErrorSummary]

  def listRecent(since: LocalDateTime, max: Int): Seq[ErrorSummary]

  def listMostFrequent(since: LocalDateTime, max: Int): Seq[ErrorSummary]
}

case class ErrorSummary(errorId: Int, code: String, occurrenceSummary: ErrorOccurrenceSummary)


case class ErrorOccurrenceSummary(error_pk: Int,
                                  firstSeen: LocalDateTime,
                                  lastSeen: LocalDateTime,
                                  recentOccurrences: Int,
                                  totalOccurrences: Int)
