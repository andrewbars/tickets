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
  def newSale(sectorID: Long, eventID:Long) = Action { implicit request =>
    val sector=Sector.getByID(sectorID)
    sector match{
      case None =>Redirect(routes.Events.show(eventID)).flashing("error"->"Задан неверный номер сектора")
      case Some(sec)=>
    	Ok(views.html.sales.sale(sec, eventID, Sector.orderedSeatsInSector(sec))(saleForm))
    	}
  }
  
  def saveSale(sectorID:Long, eventID:Long) = Action{implicit request =>
    saleForm.bindFromRequest.fold(
      formWithErrors=>
        {
          val sec=Sector.getByID(sectorID).get
          Ok(views.html.sales.sale(sec, eventID, Sector.orderedSeatsInSector(sec))(formWithErrors))
        },
      sectorMap=>{
        Sale.addNew(sectorMap, sectorID)
        Redirect(routes.Events.list()).flashing("success" -> "Сохранено!")
      }
    )
  }
}