package controllers

import play.api.mvc.Controller
import play.api.mvc.{ Action, Flash }
import models.{ Event, Sector, Seat, Sale }
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

object Sales extends Controller {

  val saleForm = Form(seatCheckboxMapping)

  def newSale(sectorID: Long, eventID: Long) = Action { implicit request =>
    val sector = Sector.getByID(sectorID)
    sector match {
      case None => Redirect(routes.Events.show(eventID)).flashing("error" -> "Задан неверный номер сектора")
      case Some(sec) =>
        Ok(views.html.sales.sale(sec, eventID, Sector.orderedSeatsInSector(sec))(saleForm))
    }
  }

  def saveSale(sectorID: Long, eventID: Long) = Action { implicit request =>
    val sec = Sector.getByID(sectorID).get
    saleForm.bindFromRequest.fold(
      formWithErrors =>
        {
          Ok(views.html.sales.sale(sec, eventID, Sector.orderedSeatsInSector(sec))(formWithErrors))
        },
      sectorMap => {
        val sale = Sale(0, eventID, new Timestamp(new Date().getTime()), sec.sitPrice, false)
        Sale.addNew(sectorMap, sectorID, sale)
        Redirect(routes.Sales.show(sale.id)).flashing("info" -> "Проверьте и подтвердите продажу")
      })
  }
  def confirmSale(saleID: Long) = Action { implicit request =>
    val sale = Sale.getByID(saleID).get
    Sale.confirmSale(sale)
    Redirect(routes.Events.show(sale.eventID)).flashing("success" -> "Продажа подтвержена!")
  }
  def revertSale(saleID: Long) = Action { implicit request =>
    val sale = Sale.getByID(saleID).get
    Sale.revertSale(sale)
    Redirect(routes.Events.show(sale.eventID)).flashing("info" -> "Продажа отменена!")
  }
  def show(saleID: Long) = Action { implicit request =>
    Sale.getByID(saleID) match {
      case None => Redirect(routes.Events.list()).flashing("error" -> "Продажа с таким ID не найдена")
      case Some(sale) => {
        val event = Sale.event(sale)
        Ok(views.html.sales.details(sale, event))
      }
    }
  }
}