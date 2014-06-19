package controllers

import play.api.mvc.Controller
import play.api.mvc.Action
import models.Event
import java.util.Date
import play.api.data.Form
import play.api.data.Forms._

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
      Redirect(routes.Events.list())
  }
  val eventMap = mapping(
    "id" -> longNumber,
    "tp" -> text,
    "name" -> text,
    "date" -> date,
    "dscr" -> text)(Event.apply)(Event.unapply)

  val eventForm = Form {
    mapping(
      "id" -> longNumber,
      "tp" -> text,
      "name" -> text,
      "date" -> date,
      "dscr" -> text)(Event.apply)(Event.unapply)
  }

  def insert = Action { implicit request =>
    eventForm.bindFromRequest.fold(
      formWithErrors => Ok(views.html.events.edit(formWithErrors)),
      success = {
        event =>
          models.Event.insert(event)
          Redirect(routes.Events.list())
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
    	formWithErrors=>Ok(views.html.events.edit(formWithErrors)),
    	success={
    	  event=>models.Event.update(event)
    	  Redirect(routes.Events.show(event.id))
    	}
      )     
  }
}