package com.briehman.errorregistry.repository.db

import com.briehman.errorregistry.models.AppError
import com.briehman.errorregistry.repository.ErrorRepository
import slick.driver.MySQLDriver.api._

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class DatabaseErrorRepository(db: Database) extends ErrorRepository {
  override def find(id: Int): Option[AppError] = {
    Await.result(db.run(AppError.table.filter(_.id === id).result.headOption), Duration.Inf)
  }

  override def find(code: String): Option[AppError] = {
    Await.result(db.run(AppError.table.filter(_.code === code).result.headOption), Duration.Inf)
  }

  override def list(ids: Seq[Int]): Seq[AppError] = {
    Await.result(db.run(AppError.table.result), Duration.Inf)
  }

  override def store(error: AppError): AppError = {
    Await.result(db.run(AppError.table += error), Duration.Inf)
    find(error.code).head
  }

  override def listCodes: Seq[String] = {
    Await.result(db.run(AppError.table.map(_.code).result), Duration.Inf)
  }
}
