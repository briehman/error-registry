package com.briehman.errorregistry

import com.briehman.errorregistry.models.{AppError, ErrorOccurrence}
import slick.driver.MySQLDriver.api._

object Driver {
  def main(args: Array[String]): Unit = {
    val db = Database.forConfig("mysql")
    try {
      val schemas = AppError.table.schema ++ ErrorOccurrence.table.schema
      val p = db.run(schemas.create)

    } catch {
      case e: Exception =>
        Console.err.println(e)
    } finally {
      db.close
    }

    println("Hooah")
  }
}
