package controllers

import play.api.mvc.Controller
import play.api.mvc.{ Action, Flash }
import models.{Event,Sector,Seat}
import java.util.{ Date, Calendar }
import java.sql.Timestamp
import java.text.{ SimpleDateFormat, ParsePosition }
import play.api.data.Form
import play.api.i18n._
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import Dates._

object Seats extends Controller {

  val sectorPriceForm = Form(mapping(
    "names" -> list(text),
    "prices" -> list(number))((names, prices) => (names zip prices).toMap)(map => Some(map.toList.unzip)))

  def setPrices(eventID: Long) = Action { implicit request =>
    Event.getSectors(eventID) match {
      case None => Redirect(routes.Events.list()).flashing("error" -> (Messages("events.notfound")))
      case Some(sectors) =>
        val sectorPrices = (for (s <- sectors) yield (s.name, s.sitPrice)).toMap
        Ok(views.html.events.prices(sectorPriceForm.fill(sectorPrices)))
    }
  }
  def savePrices(eventID: Long) = Action { implicit request =>
    sectorPriceForm.bindFromRequest.fold(
      formWithErrors => Ok(views.html.events.prices(formWithErrors)),
      pricesMap => {
        Sector.updatePrices(eventID, pricesMap)
        Redirect(routes.Events.show(eventID))
      })
  }
  
  val rowMap =mapping(
	"checkedSits"->list(boolean)
  )(checkedSits=>
    for{
      s<-checkedSits.zipWithIndex
      if s._1
    }yield (s._2+1))(numList=>Some((for(n<-(1 to 50))yield numList contains n).toList))
}