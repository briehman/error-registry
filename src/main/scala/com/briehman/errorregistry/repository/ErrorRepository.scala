package com.briehman.errorregistry.repository

import com.briehman.errorregistry.models.AppError

trait ErrorRepository {
  def find(primaryKey: Int): Option[AppError]

  def find(code: String): Option[AppError]

  def list(ids: Seq[Int]): Seq[AppError]

  def store(error: AppError): AppError

  def listCodes: Seq[String]
}


