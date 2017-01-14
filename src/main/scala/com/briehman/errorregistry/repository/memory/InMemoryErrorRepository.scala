package com.briehman.errorregistry.repository.memory

import com.briehman.errorregistry.models.AppError
import com.briehman.errorregistry.repository.ErrorRepository

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




