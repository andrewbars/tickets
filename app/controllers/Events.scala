package controllers

import play.api.mvc.Controller
import play.api.mvc.{ Action, Flash }
import models.Event
import java.util.{ Date, Calendar }
import java.sql.Timestamp
import java.text.{ SimpleDateFormat, ParsePosition }
import play.api.data.Form
import play.api.i18n._
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import Dates._

object Events extends Controller {
  def isArchive(event: Event) = Calendar.getInstance().getTime().after(event.date)

  def list(archive: Boolean = false) = Action {
    implicit request =>
      val events = if (archive)
        Event.getAll.takeWhile(isArchive(_))
      else
        Event.getAll.dropWhile(isArchive(_))
      Ok(views.html.events.list(events))
  }
  def show(id: Long) = Action {
    implicit request =>
      val e = Event.getById(id)
      e match {
        case None => Redirect(routes.Events.list()).flashing("error" -> (Messages("events.notfound")))
        case Some(x) => Ok(views.html.events.details(x))
      }
  }
  def addNew = Action {
    implicit request =>
      Ok(views.html.events.insert(eventForm))
  }
  def remove(id: Long) = Action {
    implicit request =>
      Event.getById(id) match {
        case None => Redirect(routes.Events.list()).flashing("error" -> (Messages("events.notfound")))
        case Some(x) => {
          Event.removeById(id)
          Redirect(routes.Events.list()).flashing("success" -> "Событие успешно удалено!")
        }
      }
  }

  val eventForm = Form {
    mapping(
      "id" -> longNumber,
      "tp" -> nonEmptyText.verifying(maxLength(20)),
      "name" -> nonEmptyText.verifying(maxLength(100)),
      "date" -> dateTimeMapping.verifying("Дата должна быть больше текущей", Calendar.getInstance().getTime().before(_)),
      "dscr" -> text.verifying(maxLength(500)))(Event.apply)(Event.unapply)
  }

  def insert = Action { implicit request =>
    eventForm.bindFromRequest.fold(
      formWithErrors => Ok(views.html.events.insert(formWithErrors)).flashing("error" -> "Исправьте ошибки в форме"),
      success = {
        event =>
          models.Event.insert(event)
          Redirect(routes.Events.list()).flashing("success" -> ("Событие добавлено!"))
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
  def update = Action {
    implicit request =>
      eventForm.bindFromRequest.fold(
        formWithErrors => Ok(views.html.events.edit(formWithErrors)).flashing("error" -> "Исправьте ошибки в форме"),
        success = {
          event =>
            {
              Event.getById(event.id) match {
                case None => Redirect(routes.Events.list()).flashing("error" -> (Messages("events.notfound")))
                case Some(x) => {
                  models.Event.update(event)
                  Redirect(routes.Events.show(event.id)).flashing("success" -> "Сохранено!")
                }
              }
            }
        })
  }
}