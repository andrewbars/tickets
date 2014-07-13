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
import jp.t2v.lab.play2.auth.AuthElement
import models.Permission

object Sales extends Controller with AuthElement with AuthConfigImpl {

  val saleForm = Form(
    mapping(
      "seats" -> seatCheckboxMapping)(seats => seats)(seats => Some(seats)))

  def newSale(sectorID: Long, eventID: Long) = StackAction(AuthorityKey -> Permission.default) { implicit request =>
    implicit val user = Some(loggedIn)
    val sector = Sector.getByID(sectorID)
    sector match {
      case None => Redirect(routes.Events.show(eventID)).flashing("error" -> "Задан неверный номер сектора")
      case Some(sec) =>
        Ok(views.html.sales.sale(sec, eventID, Sector.orderedSeatsInSector(sec))(saleForm))
    }
  }

  def saveSale(sectorID: Long, eventID: Long) = StackAction(AuthorityKey -> Permission.default) { implicit request =>
    implicit val user = Some(loggedIn)
    val sec = Sector.getByID(sectorID).get
    saleForm.bindFromRequest.fold(
      formWithErrors =>
        {
          Ok(views.html.sales.sale(sec, eventID, Sector.orderedSeatsInSector(sec))(formWithErrors))
        },
      sectorMap => {
        val sale = Sale(0, eventID, new Timestamp(new Date().getTime()), sec.sitPrice, false, user.get.id)
        Sale.addNew(sectorMap, sectorID, sale)
        Redirect(routes.Sales.show(sale.id)).flashing("info" -> "Проверьте и подтвердите продажу")
      })
  }
  def confirmSale(saleID: Long) = StackAction(AuthorityKey -> Permission.default) { implicit request =>
    val sale = Sale.getByID(saleID).get
    Sale.confirmSale(sale)
    Redirect(routes.Events.show(sale.eventID)).flashing("success" -> "Продажа подтвержена!")
  }
  def revertSale(saleID: Long) = StackAction(AuthorityKey -> Permission.default) { implicit request =>
    val sale = Sale.getByID(saleID).get
    if (!sale.confirmed) {
      Sale.revertSale(sale)
      Redirect(routes.Events.show(sale.eventID)).flashing("info" -> "Продажа отменена!")
    } else
      Redirect(routes.Sales.show(sale.id)).flashing("error" -> "Данная продажа уже была подтверждена!")
  }

  def cancelSale(saleID: Long) = StackAction(AuthorityKey -> Permission.editSales) { implicit request =>
    val sale = Sale.getByID(saleID).get
    Sale.revertSale(sale)
    Redirect(routes.Events.show(Sale.event(sale).id)).flashing("success" -> "Продажа отменена!")
  }

  def excludeOne(seatID: Long, saleID: Long) = StackAction(AuthorityKey -> Permission.editSales) { implicit request =>
    Seat.deleteOne(seatID)
    val sale = Sale.getByID(saleID).get
    if (Sale.seats(sale).isEmpty) {
      Sale.revertSale(sale)
      Redirect(routes.Events.show(Sale.event(sale).id)).flashing("success" -> "Продажа отменена!")
    } else
      Redirect(routes.Sales.show(sale.id)).flashing("success" -> "Выбранное место исключено")
  }
  def show(saleID: Long) = StackAction(AuthorityKey -> Permission.default) { implicit request =>
    implicit val user = Some(loggedIn)
    Sale.getByID(saleID) match {
      case None => Redirect(routes.Events.list()).flashing("error" -> "Продажа с таким ID не найдена")
      case Some(sale) => {
        val event = Sale.event(sale)
        Ok(views.html.sales.details(sale, event))
      }
    }
  }
}