package com.briehman.errorregistry.models

import java.net.URI
import java.sql.Timestamp

import com.briehman.errorregistry.message.{AppErrorMessage, ErrorOccurrenceMessage, RequestInformationMessage}
import slick.driver.MySQLDriver.api._
import slick.lifted.TableQuery

case class AppError(
                  id: Int = 0,
                  code: String,
                  error: String
                  )

object AppError extends ((Int, String, String) => AppError) {
  def apply(message: AppErrorMessage): AppError = {
    apply(code = message.code, error = message.errorString)
  }

  class AppErrors(tag: Tag) extends Table[AppError](tag, "errors") {
    def id = column[Int]("error_id", O.PrimaryKey, O.AutoInc)

    def code = column[String]("code", O.SqlType("VARCHAR(32)"))

    def error = column[String]("error")

    override def * = (id, code, error) <>(AppError.tupled, AppError.unapply)

    def uniqueCodes = index("code_unique", code, unique = true)
  }

  val table: TableQuery[AppErrors] = TableQuery[AppErrors]
}

case class ErrorOccurrence(
                            id: Int = 0,
                            error_pk: Int,
                            date: Timestamp, // TODO - replace with LocalDateTime as described in https://github.com/shekhargulati/52-technologies-in-2016/tree/master/04-slick
                            hostname: String,
                            environment: Option[String] = None,
                            issue: Option[String] = None,
                            user: Option[String] = None,
                            buildInfo: BuildInformation,
                            requestInfo: RequestInformation
                            ) {
  def transformForApi: ErrorOccurrence = {
    this.copy(environment = Some(environment.getOrElse("")),
      issue = Some(issue.getOrElse("")),
      user = Some(user.getOrElse("")),
      requestInfo = requestInfo.transformForApi())
  }
}

object ErrorOccurrence extends (
  (Int, Int, Timestamp, String, Option[String], Option[String], Option[String],
    BuildInformation, RequestInformation) => ErrorOccurrence) {

  def apply(message: ErrorOccurrenceMessage, error: AppError): ErrorOccurrence = {
    apply(error_pk = error.id, date = new Timestamp(message.date.getTime),
      hostname = message.hostname, environment = message.environment, issue = message.issue,
      buildInfo = BuildInformation(message.build, message.branch),
      requestInfo = RequestInformation(message.requestInformation))
  }

  class ErrorOccurrences(tag: Tag) extends Table[ErrorOccurrence](tag, "occurrences") {
    def id = column[Int]("occurrence_id", O.PrimaryKey, O.AutoInc)

    def errorId = column[Int]("error_id")

    def date = column[Timestamp]("date")

    def hostname = column[String]("hostname")

    def environment = column[Option[String]]("environment")

    def issue = column[Option[String]]("issue")

    def user = column[Option[String]]("user")

    def build = column[String]("build")

    def branch = column[String]("branch")

    def requestUri = column[String]("request_uri")

    def requestParameters = column[Option[String]]("request_parameters")

    def requestType = column[String]("request_type")

    def sessionId = column[Option[String]]("session_id")

    def appError = foreignKey("ERROR_FK", errorId, AppError.table)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)

    override def * = (id, errorId, date, hostname, environment, issue, user,
      (build, branch),
      (requestUri, requestType, requestParameters, sessionId)) <>(toModel, toTuple)

    private type BuildInfoTupleType = (String, String)
    private type RequestInformationTupleType = (String, String, Option[String], Option[String])
    private type ErrorOccurrenceTupleType = (Int, Int, Timestamp, String, Option[String], Option[String], Option[String], BuildInfoTupleType, RequestInformationTupleType)

    def toModel: ErrorOccurrenceTupleType => ErrorOccurrence = {
      case (id, errorId, date, hostname, environment, issue, user,
        (build, branch),
        (requestUri, requestParameters, requestType, sessionId)) =>
        ErrorOccurrence(id, errorId, date, hostname, environment, issue, user,
          BuildInformation(build, branch),
          RequestInformation(new URI(requestUri), requestParameters, requestType, sessionId))
    }

    def toTuple: ErrorOccurrence => Option[ErrorOccurrenceTupleType] = { t =>
      val buildInfo = t.buildInfo
      val requestInfo = t.requestInfo
      Some (
        (t.id, t.error_pk, t.date, t.hostname, t.environment, t.issue, t.user,
          (buildInfo.build, buildInfo.branch),
          (requestInfo.uri.toString, requestInfo.methodType, requestInfo.parameters, requestInfo.sessionId))
      )
    }
  }

  val table: TableQuery[ErrorOccurrences] = TableQuery[ErrorOccurrences]
}

case class BuildInformation(build: String, branch: String)

case class RequestInformation(uri: URI,
                              methodType: String,
                              parameters: Option[String] = None,
                              sessionId: Option[String] = None) {
  def transformForApi(): RequestInformation = {
    this.copy(
      parameters = Some(parameters.getOrElse("")),
      sessionId = Some(sessionId.getOrElse("")))
  }
}

object RequestInformation extends ((URI, String, Option[String], Option[String]) => RequestInformation) {
  def apply(message: RequestInformationMessage): RequestInformation = {
    apply(message.uri, message.methodType, message.parameters, message.sessionId)
  }
}
