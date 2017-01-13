package com.briehman.errorregistry.repository

import java.sql.Timestamp
import java.time.LocalDateTime

import com.briehman.errorregistry.boundary.{ErrorOccurrenceSummary, ErrorSummary}
import com.briehman.errorregistry.models.{AppError, ErrorOccurrence}

trait ErrorRepository {
  def find(primaryKey: Int): Option[AppError]
  def find(code: String): Option[AppError]
  def list(ids: Seq[Int]): Seq[AppError]
  def store(error: AppError): AppError
  def listCodes: Seq[String]
}

trait ErrorOccurrenceRepository {
  protected implicit def ordered[T <: Timestamp] = new Ordering[T] {
    def compare(x: T, y: T): Int = x compareTo y
  }

  protected implicit def orderedLDT[T <: LocalDateTime] = new Ordering[T] {
    def compare(x: T, y: T): Int = x compareTo y
  }

  def find(id: Int): Option[ErrorOccurrence]
  def findByCode(code: String): Seq[ErrorOccurrence]
  def store(occurrence: ErrorOccurrence): ErrorOccurrence
  def listUniqueNew(since: LocalDateTime, max: Int): Seq[ErrorOccurrenceSummary]
  def listUniqueNewer(since: LocalDateTime, max: Int): Seq[ErrorSummary]
  def listUniqueRecent(since: LocalDateTime, max: Int): Seq[ErrorOccurrenceSummary]
  def listUniqueMostFrequent(since: LocalDateTime, max: Int): Seq[ErrorOccurrenceSummary]
}
