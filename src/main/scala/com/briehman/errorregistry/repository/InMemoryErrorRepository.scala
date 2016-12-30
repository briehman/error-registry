package com.briehman.errorregistry.repository

import java.sql.Timestamp
import java.time.LocalDateTime

import com.briehman.errorregistry.boundary.ErrorOccurrenceSummary
import com.briehman.errorregistry.models.{AppError, ErrorOccurrence}

class InMemoryErrorRepository extends ErrorRepository {
  private var codes = Map[String, AppError]()

  override def find(primaryKey: Int): Option[AppError] = codes.values.find(_.id == primaryKey)

  override def find(code: String): Option[AppError] = codes.get(code)

  override def list(ids: Seq[Int]): Seq[AppError] = {
    codes.values.filter(ids contains _.id).toSeq
  }

  override def store(error: AppError): AppError = {
    if (codes.contains(error.code)) {
      throw new IllegalArgumentException("Cannot double persist")
    }
    val storedError = error.copy(id = codes.size)
    codes = codes + (error.code -> storedError)
    storedError
  }

  override def listCodes: Seq[String] = codes.keys.toSeq
}

class InMemoryErrorOccurrenceRepository(errorRepository: ErrorRepository)
  extends ErrorOccurrenceRepository {
  private var occurrences = Map[Int, ErrorOccurrence]()

  private implicit def ordered[T <: Timestamp] = new Ordering[T] {
    def compare(x: T, y: T): Int = x compareTo y
  }

  private implicit def orderedLDT[T <: LocalDateTime] = new Ordering[T] {
    def compare(x: T, y: T): Int = x compareTo y
  }

  override def find(id: Int): Option[ErrorOccurrence] = occurrences.get(id)

  override def findByCode(code: String): Seq[ErrorOccurrence] = {
    errorRepository.find(code) match {
      case None => List()
      case Some(f) => findById(f.id)
    }
  }

  private def findById(id: Int) = {
    occurrences.filter(t => t._2.error_pk == id).values.toList
  }

  override def store(occurrence: ErrorOccurrence): ErrorOccurrence = {
    val stored = occurrence.copy(id = occurrences.size)
    occurrences = occurrences + (stored.id -> stored)
    stored
  }

  override def listUniqueNew(since: LocalDateTime, max: Int): Seq[ErrorOccurrenceSummary] = {
    getOccurrenceSummariesSince(since)
      .filter(o => o.firstSeen.isAfter(since))
      .sortBy(_.firstSeen)(Ordering[LocalDateTime].reverse)
      .take(max)
  }

  override def listUniqueRecent(since: LocalDateTime, max: Int): Seq[ErrorOccurrenceSummary] = {
    getOccurrenceSummariesSince(since)
      .sortBy(_.lastSeen)(Ordering[LocalDateTime].reverse)
      .take(max)
  }

  override def listUniqueMostFrequent(since: LocalDateTime, max: Int): Seq[ErrorOccurrenceSummary] = {
    getOccurrenceSummariesSince(since)
      .sortBy(_.totalOccurrences)(Ordering[Int].reverse)
      .take(max)
  }

  private def getOccurrenceSummariesSince(date: LocalDateTime): List[ErrorOccurrenceSummary] = {
    occurrences
      .filter { case (id, occurrence) => occurrence.date.toLocalDateTime.isAfter(date) }
      .groupBy { case (id, occurrence) => occurrence.error_pk }
      .map { case (id, occurrenceMap) =>
        val errorOccurrences = occurrences.values.filter(_.error_pk == id)
        val firstSeen = errorOccurrences.minBy(_.date).date
        val lastSeen = errorOccurrences.maxBy(_.date).date
        ErrorOccurrenceSummary(id, firstSeen.toLocalDateTime, lastSeen.toLocalDateTime, errorOccurrences.size)
      }
      .toList
  }
}
