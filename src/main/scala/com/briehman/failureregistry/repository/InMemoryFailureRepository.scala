package com.briehman.failureregistry.repository

import com.briehman.failureregistry.models.{FailureOccurrence, Failure}

class InMemoryFailureRepository extends FailureRepository {
  var codes = Map[String, Failure]()

  override def find(code: String): Option[Failure] = codes.get(code)

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
  var occurrences  = Map[Int, FailureOccurrence]()

  override def find(id: Int): Option[FailureOccurrence] = occurrences.get(id)

  override def findByFailure(code: String): List[FailureOccurrence] = {
    failureRepository.find(code) match {
      case None => List()
      case Some(f) => findByFailureId(f.id)
    }
  }

  def findByFailureId(id: Int) = {
    occurrences.filter(t => t._2.failure_pk == id).values.toList
  }

  override def store(occurrence: FailureOccurrence): FailureOccurrence =  {
    val stored = occurrence.copy(id = occurrences.size)
    occurrences = occurrences + (stored.id -> stored)
    stored
  }
}
