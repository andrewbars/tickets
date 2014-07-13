package controllers

import play.api.mvc.Controller
import play.api.mvc.{ Action, Flash }
import models.{ Event, Sector, Seat }
import java.util.{ Date, Calendar }
import java.sql.Timestamp
import java.text.{ SimpleDateFormat, ParsePosition }
import play.api.data.Form
import play.api.i18n._
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import Dates._
import jp.t2v.lab.play2.auth.AuthElement
import models.Permission

object Seats extends Controller with AuthElement with AuthConfigImpl {

  val rowMap = mapping(
    "checkedSeats" -> list(boolean))(checkedSeats =>
      for {
        s <- checkedSeats.zipWithIndex
        if s._1
      } yield (s._2 + 1))(numList => Some((for (n <- (1 to 50)) yield numList contains n).toList))

  val sectorPriceForm = Form(mapping(
    "names" -> list(text),
    "prices" -> list(number))((names, prices) => (names zip prices).toMap)(priceMap =>
      Some((for (name <- Sector.sectorNames) yield (name, priceMap(name))).unzip)))

  def setPrices(implicit eventID: Long) = StackAction(AuthorityKey -> Permission.editEvent) { implicit request =>
    implicit val user = Some(loggedIn)
    Event.getSectors(eventID) match {
      case None => Redirect(routes.Events.list()).flashing("error" -> (Messages("events.notfound")))
      case Some(sectors) =>
        val sectorPrices = (for (s <- (sectors)) yield (s.name, s.sitPrice)).toMap
        Ok(views.html.events.prices(sectorPriceForm.fill(sectorPrices)))
    }
  }
  def savePrices(implicit eventID: Long) = StackAction(AuthorityKey -> Permission.editEvent) { implicit request =>
    implicit val user = Some(loggedIn)
    sectorPriceForm.bindFromRequest.fold(
      formWithErrors => Ok(views.html.events.prices(formWithErrors)),
      pricesMap => {
        Sector.updatePrices(eventID, pricesMap)
        Redirect(routes.Events.show(eventID))
      })
  }

  val seatCheckForm = Form(
    mapping(
      "event" -> longNumber,
      "sector" -> text,
      "row" -> number,
      "num" -> number)((event, sector, row, num) => (event, sector, row, num))(tup => Some(tup)))

  val seatCheckboxMapping = mapping(
    "rows" -> list(rowMap))(rows => rows)(rows => Some(rows)).verifying("Выберете хотя бы одно место!", _ exists (_.nonEmpty))

  def checkSeat = StackAction(AuthorityKey -> Permission.default) { implicit request =>
    implicit val user = Some(loggedIn)
    Ok(views.html.seats.seatcheck(seatCheckForm))
  }

  def checkSeatProc = StackAction(AuthorityKey -> Permission.default) { implicit request =>
    implicit val user = Some(loggedIn)
    seatCheckForm.bindFromRequest.fold(
      formWithErrors => Ok(views.html.seats.seatcheck(formWithErrors)),
      seatParams => {
        val event = Event.getById(seatParams._1).get
        val sector = Sector.findByNameFromEvent(event, seatParams._2).get
        val seats = Sector.seats(sector)
        val result =
          seats.find(seat => seat.rowNumber == seatParams._3
            && seat.num == seatParams._4) match {
            case None => ("info" -> "Free Seat!")
            case Some(seat) => ("error" -> "Место продано!")
          }
        Redirect(routes.Seats.checkSeat).flashing(result)
      })
  }
}