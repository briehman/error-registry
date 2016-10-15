package com.briehman.failureregistry.repository

import com.briehman.failureregistry.models.{FailureOccurrence, Failure}

trait FailureRepository {
  def find(code: String): Option[Failure]
  def store(failure: Failure): Failure
  def listCodes: Seq[String]
}

trait FailureOccurrenceRepository {
  def find(id: Int): Option[FailureOccurrence]
  def findByFailure(code: String): List[FailureOccurrence]
  def store(occurrence: FailureOccurrence): FailureOccurrence
}
