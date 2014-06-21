package controllers

import play.api.mvc.Controller
import play.api.mvc.{Action, Flash}
import models.Event
import java.util.{Date}
import java.sql.Timestamp
import java.text.{SimpleDateFormat,ParsePosition}
import play.api.data.Form
import play.api.data.Forms._
import Dates._

object Events extends Controller {
  def list = Action {
    implicit request =>
      val events = Event.getAll
      Ok(views.html.events.list(events))
  }
  def show(id: Long) = Action {
    implicit request =>
      val e = Event.getById(id)
      e match {
        case None => NotFound
        case Some(x) => Ok(views.html.events.details(x))
      }
  }
  def addNew = Action {
    implicit request =>
      Ok(views.html.events.insert(eventForm))
  }
  def remove(id: Long) = Action {
    implicit request =>
      Event.removeById(id)
      Redirect(routes.Events.list()).flashing("success"->"Событие успешно удалено!")
  }
  
  val eventForm = Form {
    mapping(
      "id"->longNumber,
      "tp" -> text,
      "name" -> text,
      "date" -> dateTimeMapping,
      "dscr" -> text)(Event.apply)(Event.unapply)
  }

  def insert = Action { implicit request =>
    eventForm.bindFromRequest.fold(
      formWithErrors => Ok(views.html.events.insert(formWithErrors)).flashing("error"->"Исправьте ошибки в форме"),
      success = {
        event =>
          models.Event.insert(event)
          Redirect(routes.Events.list()).flashing("success"->("Событие добавлено!"))
      })
  }

  def edit(id: Long) = Action {
    implicit request =>
      val event = Event.getById(id)
      event match {
        case None => NotFound
        case Some(x) => Ok(views.html.events.edit(eventForm.fill(x)))
      }
  }
  def update = Action{
    implicit request=>
      eventForm.bindFromRequest.fold(
    	formWithErrors=>Ok(views.html.events.edit(formWithErrors)).flashing("error"->"Исправьте ошибки в форме"),
    	success={
    	  event=>models.Event.update(event)
    	  Redirect(routes.Events.show(event.id)).flashing("success"->"Сохранено!")
    	}
      )     
  }
}