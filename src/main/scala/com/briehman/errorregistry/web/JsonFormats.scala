package com.briehman.errorregistry.web

import java.net.URI
import java.sql.{Date, Timestamp}

import org.json4s.CustomSerializer
import org.json4s.JsonAST.{JInt, JNull, JString}

case object DateSerializer extends CustomSerializer[java.sql.Date](format => ( {
  case JString(s) => Date.valueOf(s)
  case JNull => null
}, {
  case d: Date => JString(d.toString)
}))

case object CustomTimestampSerializer extends CustomSerializer[Timestamp](format => ( {
  case JInt(x) => new Timestamp(x.longValue * 1000)
  case JNull => null
}, {
  case date: Timestamp => JInt(date.getTime / 1000)
}))

case object UriSerializer extends CustomSerializer[URI](format => ( {
  case JString(s) => new URI(s)
  case JNull => null
}, {
  case uri: URI => JString(uri.toString)
}))