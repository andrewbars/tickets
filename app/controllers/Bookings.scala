package controllers

import play.api.mvc.Controller
import play.api.mvc.{ Action, Flash }
import models.{ Event, Sector, Seat, Sale, Booking }
import java.util.{ Date, Calendar }
import java.sql.Timestamp
import java.text.{ SimpleDateFormat, ParsePosition }
import play.api.data.Form
import play.api.i18n._
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import Dates._
import Seats._
import com.mysql.jdbc.NotImplemented

object Bookings extends Controller {

  val bookingForm=Form(mapping(
	"expDate"->dateTimeMapping,
	"clientName"->nonEmptyText,
	"seats"->seatCheckboxMapping
  )((expDate,clientName, seats)=>(expDate,clientName, seats))(book=>Some(book)))
  def newBooking(sectorID: Long, eventID: Long) = Action { implicit request =>
    val sector = Sector.getByID(sectorID)
    sector match {
      case None => Redirect(routes.Events.show(eventID)).flashing("error" -> "Задан неверный номер сектора")
      case Some(sec) =>
        //TODO Replace with actual booking view
        NotImplemented
    }
  }
  
  def saveBooking(sectorID: Long, eventID: Long) = Action { implicit request =>
    val sec = Sector.getByID(sectorID).get
    bookingForm.bindFromRequest.fold(
      formWithErrors =>
        {
          //TODO Replace with actual booking view
          NotImplemented
        },
      sectorMap => {
        val booking = Booking(0, eventID, new Timestamp(new Date().getTime()), sectorMap._2, sectorMap._1, sec.sitPrice, false)
        Booking.addNew(sectorMap._3, sectorID, booking)
        //TODO Replace with actual booking view
        NotImplemented
      })
  }
}