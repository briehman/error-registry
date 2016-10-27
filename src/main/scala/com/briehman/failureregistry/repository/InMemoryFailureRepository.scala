package com.briehman.failureregistry.repository

import java.sql.Timestamp
import java.time.LocalDateTime

import com.briehman.failureregistry.models.{Failure, FailureOccurrence}

class InMemoryFailureRepository extends FailureRepository {
  var codes = Map[String, Failure]()

  override def find(primaryKey: Int): Option[Failure] = codes.values.find(_.id == primaryKey)

  override def find(code: String): Option[Failure] = codes.get(code)

  override def list(ids: Seq[Int]): Seq[Failure] = {
    codes.values.filter(ids contains _.id).toSeq
  }

  override def store(failure: Failure): Failure = {
    if (codes.contains(failure.code)) {
      throw new IllegalArgumentException("Cannot double persist")
    }
    val storedFailure = failure.copy(id = codes.size)
    codes = codes + (failure.code -> storedFailure)
    storedFailure
  }

  override def listCodes: Seq[String] = codes.keys.toSeq
}

class InMemoryFailureOccurrenceRepository(failureRepository: FailureRepository)
  extends FailureOccurrenceRepository {
  var occurrences = Map[Int, FailureOccurrence]()

  private implicit def ordered[T <: Timestamp] = new Ordering[T] {
    def compare(x: T, y: T): Int = x compareTo y
  }

  private implicit def orderedLDT[T <: LocalDateTime] = new Ordering[T] {
    def compare(x: T, y: T): Int = x compareTo y
  }

  override def find(id: Int): Option[FailureOccurrence] = occurrences.get(id)

  override def findByFailure(code: String): Seq[FailureOccurrence] = {
    failureRepository.find(code) match {
      case None => List()
      case Some(f) => findByFailureId(f.id)
    }
  }

  def findByFailureId(id: Int) = {
    occurrences.filter(t => t._2.failure_pk == id).values.toList
  }

  override def store(occurrence: FailureOccurrence): FailureOccurrence = {
    val stored = occurrence.copy(id = occurrences.size)
    occurrences = occurrences + (stored.id -> stored)
    stored
  }

  override def listUniqueRecentOccurrences(since: LocalDateTime, max: Int): Seq[FailureOccurrenceSummary] = {
    getOccurrenceSummariesSince(since)
      .sortBy(_.lastSeen)
      .takeRight(max)
  }

  override def listTopOccurrences(since: LocalDateTime, max: Int): Seq[FailureOccurrenceSummary] = {
    getOccurrenceSummariesSince(since)
      .sortBy(_.totalOccurrences)
      .takeRight(max)
  }

  private def getOccurrenceSummariesSince(date: LocalDateTime): List[FailureOccurrenceSummary] = {
    occurrences
      .filter { case (id, occurrence) => occurrence.date.toLocalDateTime.isAfter(date) }
      .groupBy { case (id, occurrence) => occurrence.failure_pk }
      .map { case (id, occurrenceMap) =>
        val failureOccurrences = occurrences.values.filter(_.failure_pk == id)
        val firstSeen = failureOccurrences.minBy(_.date).date
        val lastSeen = failureOccurrences.maxBy(_.date).date
        FailureOccurrenceSummary(id, firstSeen.toLocalDateTime, lastSeen.toLocalDateTime, failureOccurrences.size)
      }
      .toList
  }
}
