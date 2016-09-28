package models

import java.sql.Date

import slick.driver.H2Driver.api._
import slick.lifted.Rep

case class Failure(
                  id: Int,
                  date: Date
                  )

class Failures(tag: Tag) extends Table[Failure](tag, "failures") {
  def id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)

  def date: Rep[Date] = column[Date]("date")

  override def * = (id, date).shaped <> (Failure.tupled, Failure.unapply)
}

object FailuresDAO extends TableQuery(new Failures(_)) {

}