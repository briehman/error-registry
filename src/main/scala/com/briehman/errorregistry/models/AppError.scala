package com.briehman.errorregistry.models

import java.sql.Timestamp

import slick.driver.MySQLDriver.api._
import slick.lifted.TableQuery

case class AppError(
                  id: Int = 0,
                  code: String
                  )

object AppError extends ((Int, String) => AppError) {
  class AppErrors(tag: Tag) extends Table[AppError](tag, "errors") {
    def id = column[Int]("error_id", O.PrimaryKey, O.AutoInc)

    def code = column[String]("code", O.SqlType("VARCHAR(32)"))

    override def * = (id, code) <>(AppError.tupled, AppError.unapply)

    def uniqueCodes = index("code_unique", code, unique = true)
  }

  val table: TableQuery[AppErrors] = TableQuery[AppErrors]
}

case class ErrorOccurrence(
                            id: Int = 0,
                            error_pk: Int,
                            date: Timestamp // TODO - replace with LocalDateTime as described in https://github.com/shekhargulati/52-technologies-in-2016/tree/master/04-slick
                            )

object ErrorOccurrence extends ((Int, Int, Timestamp) => ErrorOccurrence) {
  class ErrorOccurrences(tag: Tag) extends Table[ErrorOccurrence](tag, "occurrences") {
    def id = column[Int]("occurrence_id", O.PrimaryKey, O.AutoInc)

    def errorId = column[Int]("error_id")

    def date = column[Timestamp]("date")

    def appError = foreignKey("ERROR_FK", errorId, AppError.table)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)

    override def * = (id, errorId, date) <>(ErrorOccurrence.tupled, ErrorOccurrence.unapply)
  }

  val table: TableQuery[ErrorOccurrences] = TableQuery[ErrorOccurrences]
}
