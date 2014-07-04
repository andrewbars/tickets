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
  val saleForm = Form {
    mapping(
      "rows" -> list(rowMap)
      )(rows => rows)(rows => Some(rows))
  }
  
  val seatCheckForm = Form(
      mapping (
    	"event"-> longNumber,
    	"sector"-> text,
    	"row"->number,
    	"num"->number
      )((event,sector,row,num)=>(event,sector,row,num))(tup=>Some(tup))
  )
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
        val sale = Sale(0, eventID, new Timestamp(new Date().getTime()))
        Sale.addNew(sectorMap, sectorID, sale)
        Redirect(routes.Sales.show(sale.id)).flashing("success" -> "Сохранено!")
      }
    )
  }
  def show(saleID:Long)=Action{implicit request=>
  	Sale.getByID(saleID) match{
  	  case None=> Redirect(routes.Events.list()).flashing("error" -> "Продажа с таким ID не найдена")
  	  case Some(sale)=>{
  	    val event=Sale.event(sale)
  	    Ok(views.html.sales.details(sale,event))
  	  }
  	}
  }
  def checkSeat=Action{implicit request=>
    seatCheckForm.bindFromRequest.fold(
      formWithErrors=>NotImplemented,
      seatParams=>{
        val event=Event.getById(seatParams._1)
        event match{
          case None=>NotImplemented
          case Some(event)=>{
            val sector=Sector.findByNameFromEvent(event, seatParams._2).get
            val seats=Sector.seats(sector)
            seats.find(seat=> seat.rowNumber==seatParams._3
                && seat.num==seatParams._4) match{
              case None=>null
              case Some(seat)=>null
            }
          }
        }
        NotImplemented
      }
    )
    
  }
}