package com.briehman.errorregistry.web

import org.json4s.JsonAST.JNull
import org.json4s.{CustomSerializer, DefaultFormats, Formats}
import org.scalatra.json.JacksonJsonSupport

object AppErrorJsonFormats {

  class NoneJNullSerializer extends CustomSerializer[Option[_]](_ => ( {
    case JNull => None
  }, {
    case None => JNull
  }))

  val appErrorJsonFormats: Formats = (DefaultFormats + DateSerializer
    + CustomTimestampSerializer + UriSerializer + new NoneJNullSerializer)
}

trait AppErrorJsonFormats extends JacksonJsonSupport {
  override protected implicit def jsonFormats: Formats = AppErrorJsonFormats.appErrorJsonFormats
}
