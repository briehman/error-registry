package com.briehman.errorregistry

import com.briehman.errorregistry.models.{AppError, ErrorOccurrence}
import slick.driver.MySQLDriver.api._

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

object Driver {
  def main(args: Array[String]): Unit = {
    val db = Database.forConfig("mysql")
    try {
      val a = (AppError.table.schema ++ ErrorOccurrence.table.schema).create
      val p = db.run(a)

//      p.foreach { s => println(s) }
//      Await.ready(db.run(errors.schema.create), Duration.Inf)
//      val result = errors.result
//      db.stream(result).foreach(println)
    } catch {
      case e: Exception =>
        Console.err.println(e)
    } finally {
      db.close
    }

    println("Hooah")
  }
}
