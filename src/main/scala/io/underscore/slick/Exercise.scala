package io.underscore.slick

import scala.slick.driver.SQLiteDriver.simple._
import org.joda.time.DateTime
import java.sql.Timestamp


trait Exercise extends App {

  implicit def dateTime  =
      MappedColumnType.base[DateTime, Timestamp](
        dt => new Timestamp(dt.getMillis),
        ts => new DateTime(ts.getTime)
  )
  
}