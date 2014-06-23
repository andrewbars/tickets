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
  def saleForm = Form {
    mapping(
      "rows" -> list(rowMap)
      )(rows => rows)(rows => Some(rows))
  }
  def newSale = Action { implicit request =>
    NotImplemented
  }
  
  def saveSale(sectorID:Long) = Action{implicit request =>
    saleForm.bindFromRequest.fold(
      formWithErrors=>NotImplemented,
      sectorMap=>{
        Sale.addNew(sectorMap, sectorID)
        Redirect(routes.Events.list()).flashing("success" -> "Сохранено!")
      }
    )
  }
}