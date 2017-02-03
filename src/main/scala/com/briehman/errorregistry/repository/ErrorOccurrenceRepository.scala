package com.briehman.errorregistry.repository

import java.sql.Timestamp
import java.time.LocalDateTime

import com.briehman.errorregistry.boundary.{AppErrorDetailStats, ErrorSummary}
import com.briehman.errorregistry.models.ErrorOccurrence

trait ErrorOccurrenceRepository {
  protected implicit def timestampOrderAsc[T <: Timestamp] = new Ordering[T] {
    def compare(x: T, y: T): Int = x compareTo y
  }

  protected implicit def orderedLDT[T <: LocalDateTime] = new Ordering[T] {
    def compare(x: T, y: T): Int = x compareTo y
  }

  def find(id: Int): Option[ErrorOccurrence]

  def findByCode(code: String): Seq[ErrorOccurrence]

  def store(occurrence: ErrorOccurrence): ErrorOccurrence

  def listUniqueNew(since: LocalDateTime, max: Int): Seq[ErrorSummary]

  def listUniqueRecent(since: LocalDateTime, max: Int): Seq[ErrorSummary]

  def listUniqueMostFrequent(since: LocalDateTime, max: Int): Seq[ErrorSummary]

  def getStatsByAppError(errorId: Int): Option[AppErrorDetailStats]

  def findByError(appErrorId: Int, startAt: Int, maxResults: Int): Seq[ErrorOccurrence]

  def countErrorOccurrences(appErrorId: Int): Int
}