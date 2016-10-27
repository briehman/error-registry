package com.briehman.failureregistry.repository

import java.time.LocalDateTime

import com.briehman.failureregistry.models.{Failure, FailureOccurrence}

trait FailureRepository {
  def find(primaryKey: Int): Option[Failure]
  def find(code: String): Option[Failure]
  def list(ids: Seq[Int]): Seq[Failure]
  def store(failure: Failure): Failure
  def listCodes: Seq[String]
}

trait FailureOccurrenceRepository {
  def find(id: Int): Option[FailureOccurrence]
  def findByFailure(code: String): Seq[FailureOccurrence]
  def store(occurrence: FailureOccurrence): FailureOccurrence
  def listUniqueRecentOccurrences(since: LocalDateTime, max: Int): Seq[FailureOccurrenceSummary]
  def listTopOccurrences(since: LocalDateTime, max: Int): Seq[FailureOccurrenceSummary]
}

case class FailureOccurrenceSummary(failure_pk: Int,
                                    firstSeen: LocalDateTime,
                                    lastSeen: LocalDateTime,
                                    totalOccurrences: Int)
