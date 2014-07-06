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
        Ok(views.html.bookings.booking(sec, eventID,Sector.orderedSeatsInSector(sec))(bookingForm))
    }
  }
  
  def saveBooking(sectorID: Long, eventID: Long) = Action { implicit request =>
    val sec = Sector.getByID(sectorID).get
    bookingForm.bindFromRequest.fold(
      formWithErrors =>
        {
          Ok(views.html.bookings.booking(sec, eventID,Sector.orderedSeatsInSector(sec))(formWithErrors))
        },
      sectorMap => {
        val booking = Booking(0, eventID, new Timestamp(new Date().getTime()), sectorMap._2, sectorMap._1, sec.sitPrice, false)
        Booking.addNew(sectorMap._3, sectorID, booking)
        Redirect(routes.Bookings.show(booking.id)).flashing("info" -> "Проверьте и подтвердите бронирование")
      })
  }
  def confirmBooking(bookingID:Long)=Action{implicit request=>
    val booking =Booking.getById(bookingID).get
    Booking.confirmBooking(booking)
    Redirect(routes.Events.show(booking.eventID)).flashing("success" -> "Бронирование подтверждено!")
  }
  def revertBooking(bookingID:Long)=Action{implicit request=>
  	val booking =Booking.getById(bookingID).get
    Booking.revertBooking(booking)
    Redirect(routes.Events.show(booking.eventID)).flashing("info" -> "Бронирование отменено!")
  }
  def show(bookingID:Long)=Action{implicit request=>
  	Booking.getById(bookingID) match{
  	  case None=> Redirect(routes.Events.list()).flashing("error" -> "Бронирование с таким ID не найдена")
  	  case Some(booking)=>{
  	    val event=Booking.event(booking)
  	    Ok(views.html.bookings.details(booking,event))
  	  }
  	}
  }
}