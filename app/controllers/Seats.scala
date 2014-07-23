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

  val seatCheckboxMapping = mapping(
    "rows" -> list(rowMap))(rows => rows)(rows => Some(rows)).verifying("Выберете хотя бы одно место!", _ exists (_.nonEmpty))

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

  def seatRelatedOrder(eventID: Long, sectorID: Long, row: Int, seatNum: Int) = StackAction(AuthorityKey -> Permission.default) { implicit request =>
    implicit val user = Some(loggedIn)
    try {
      val sectorOpt = Sector.getByID(sectorID)
      if (!sectorOpt.isDefined)
        Redirect(routes.Events.list).flashing("error" -> "Задан неверный номер сектора")
      val seatOpt = Sector.seats(Sector.getByID(sectorID).get) find (s => s.num == seatNum && s.rowNumber == row)
      seatOpt match {
        case None => Redirect(routes.Events.showWithSector(eventID, sectorID)).flashing("info" -> "Данное место свободно")
        case Some(seat) =>
          if (seat.sold)
            Redirect(routes.Sales.show(seat.saleID.get))
          else
            Redirect(routes.Bookings.show(seat.bookingID.get))
      }
    } catch {
      case e: NoSuchElementException => BadRequest(views.html.badRequest(request.flash))
    }
  }
}