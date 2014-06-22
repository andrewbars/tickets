package controllers

import java.util.{ Date }
import java.sql.Timestamp
import java.text.{ SimpleDateFormat, ParsePosition }
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import scala.util.matching._

object Dates {

  val dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm")
  val dateFormat = new SimpleDateFormat("yyyy-MM-dd")
  val timeFormat = new SimpleDateFormat("HH:mm")

  val dateTimeMapping = mapping(
    "date" -> nonEmptyText.verifying(pattern("""\d\d\d\d-\d\d-\d\d""".r, "ГГГГ-ММ-ДД", "Дата должна быть в формате ГГГГ-ММ-ДД")),
    "time" -> nonEmptyText.verifying(pattern("""\d\d:\d\d""".r,
      "ЧЧ:ММ",
      "Время должно быть в формате ЧЧ:ММ")))((date, time) => {
      val dateTime = date + " " + time
      new Timestamp(dateTimeFormat.parse(dateTime, new ParsePosition(0)).getTime())
    })(date => {
      Some(dateFormat.format(date), timeFormat.format(date))
    })
}