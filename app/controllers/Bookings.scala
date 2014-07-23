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
import jp.t2v.lab.play2.auth.AuthElement
import models.Permission
import General.searchForm

object Bookings extends Controller with AuthElement with AuthConfigImpl {

  val bookingForm = Form(mapping(
    "expDate" -> dateTimeMapping.verifying("Срок истечения не может быть меньше текущего времени!", _.after(new Timestamp(Calendar.getInstance().getTimeInMillis()))),
    "clientName" -> nonEmptyText,
    "seats" -> seatCheckboxMapping)((expDate, clientName, seats) => (expDate, clientName, seats))(book => Some(book)))

  def newBooking(sectorID: Long, eventID: Long) = StackAction(AuthorityKey -> Permission.default) { implicit request =>
    implicit val user = Some(loggedIn)
    val sector = Sector.getByID(sectorID)
    sector match {
      case None => Redirect(routes.Events.show(eventID)).flashing("error" -> "Задан неверный номер сектора")
      case Some(sec) =>
        val event = Sector.event(sec)
        val expTime = new Timestamp(event.date.getTime() - event.bookingExpTime * 60 * 1000)
        Ok(views.html.bookings.booking(sec, eventID, Sector.orderedSeatsInSector(sec))(bookingForm.fill((expTime, "", Nil))))
    }
  }

  def saveBooking(sectorID: Long, eventID: Long) = StackAction(AuthorityKey -> Permission.default) { implicit request =>
    implicit val user = Some(loggedIn)
    val sec = Sector.getByID(sectorID).get
    bookingForm.bindFromRequest.fold(
      formWithErrors =>
        {
          Ok(views.html.bookings.booking(sec, eventID, Sector.orderedSeatsInSector(sec))(formWithErrors))
        },
      sectorMap => {
        val booking = Booking(0, eventID, new Timestamp(new Date().getTime()), sectorMap._2, sectorMap._1, sec.sitPrice, false, user.get.id)
        Booking.addNew(sectorMap._3, sectorID, booking)
        Redirect(routes.Bookings.show(booking.id)).flashing("info" -> "Проверьте и подтвердите бронирование")
      })
  }
  def confirmBooking(bookingID: Long) = StackAction(AuthorityKey -> Permission.default) { implicit request =>
    Booking.getById(bookingID) match {
      case None => Redirect(routes.Events.list).flashing("error" -> "Бронирование с таким ID не найдено")
      case Some(booking) =>
        Booking.confirmBooking(booking)
        Redirect(routes.Events.show(booking.eventID)).flashing("success" -> "Бронирование подтверждено!")
    }

  }
  def revertBooking(bookingID: Long) = StackAction(AuthorityKey -> Permission.default) { implicit request =>
    implicit val user = Some(loggedIn)
    Booking.getById(bookingID) match {
      case None => Redirect(routes.Events.list).flashing("error" -> "Бронирование с таким ID не найдено")
      case Some(booking) =>
        Booking.revertBooking(booking)
        Redirect(routes.Events.show(booking.eventID)).flashing("info" -> "Бронирование отменено!")
    }
  }

  def redeemBooking(bookingID: Long) = StackAction(AuthorityKey -> Permission.default) { implicit request =>
    try{
      val booking = Booking.getById(bookingID).get
	    val sale = Sale(0,
	      Booking.event(booking).id,
	      new Timestamp(Calendar.getInstance().getTimeInMillis()),
	      booking.price,
	      true,
	      loggedIn.id,
	      true,
	      Some(booking.userID))
	    Booking.redeemBooking(booking, sale)
	    Redirect(routes.Sales.show(sale.id)).flashing("success" -> "Выкуп продажи успешно подтвержден!")
    }catch{
      case e:NoSuchElementException=>BadRequest(views.html.badRequest(request.flash))
    }
  }

  def show(bookingID: Long) = StackAction(AuthorityKey -> Permission.default) { implicit request =>
    implicit val user = Some(loggedIn)
    Booking.getById(bookingID) match {
      case None => Redirect(routes.Events.list()).flashing("error" -> "Бронирование с таким ID не найдена")
      case Some(booking) => {
        val event = Booking.event(booking)
        Ok(views.html.bookings.details(booking, event))
      }
    }
  }

  def find = StackAction(AuthorityKey -> Permission.default) { implicit request =>
    implicit val user = Some(loggedIn)
    Ok(views.html.bookings.search(searchForm))
  }

  def findProc = StackAction(AuthorityKey -> Permission.default) { implicit request =>
    implicit val user = Some(loggedIn)
    searchForm.bindFromRequest.fold(
      formWithErrors => Ok(views.html.bookings.search(formWithErrors)),
      searchVal => {
        try {
          val bookingID = searchVal.toLong
          Booking.getById(bookingID) match {
            case None => Redirect(routes.Bookings.find).flashing("error" -> "Бронирование с таким ID не найдено")
            case Some(booking) => Redirect(routes.Bookings.show(booking.id))
          }
        } catch {
          case e: java.lang.NumberFormatException =>
            val bookings = Booking.findByClientName(searchVal)
            if (bookings.isEmpty)
              Redirect(routes.Bookings.find).flashing("error" -> "По Вашему запросу ничего не найдено")
            else
              Ok(views.html.bookings.list(bookings, searchForm))
        }
      })
  }
  def excludeOne(seatID: Long, bookingID: Long) = StackAction(AuthorityKey -> Permission.default) { implicit request =>
    try{
	    Seat.deleteOne(seatID)
	    val booking = Booking.getById(bookingID).get
	    if (Booking.seats(booking).isEmpty) {
	      Booking.revertBooking(booking)
	      Redirect(routes.Events.list).flashing("success" -> "Бронирование отменено")
	    } else {
	      Redirect(routes.Bookings.show(bookingID)).flashing("success" -> "Выбранное место исключено")
	    }
    }catch{
      case e:NoSuchElementException=>BadRequest(views.html.badRequest(request.flash))
    }
  }
}