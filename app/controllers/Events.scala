package controllers

import play.api.mvc.Controller
import play.api.mvc.{ Action, Flash }
import models.{ Event, Sector, User }
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


object Events extends Controller with AuthElement with AuthConfigImpl {
  def isArchive(event: Event) = Calendar.getInstance().getTime().after(event.date)

  def list(archive: Boolean = false) = StackAction(AuthorityKey->Permission.default) {
    implicit request =>
      implicit val user = Some(loggedIn)
      val events = if (archive)
        Event.getAll.takeWhile(isArchive(_))
      else
        Event.getAll.dropWhile(isArchive(_))
      Ok(views.html.events.list(events))
  }
  def show(id: Long, sectorID: Long) = StackAction(AuthorityKey->Permission.default) {
    implicit request =>
      implicit val user = Some(loggedIn)
      val e = Event.getById(id)
      e match {
        case None => Redirect(routes.Events.list()).flashing("error" -> (Messages("events.notfound")))
        case Some(event) => {
          val sec = Sector.getByID(sectorID)
          Ok(views.html.events.details(event, sec))
        }
      }
  }
  def addNew = StackAction(AuthorityKey->Permission.editEvent) {
    implicit request =>
      implicit val user = Some(loggedIn)
      Ok(views.html.events.insert(eventForm))
  }
  def remove(id: Long) = StackAction(AuthorityKey->Permission.editEvent) {implicit request =>
      implicit val user = Some(loggedIn)
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
      "name" -> nonEmptyText.verifying(maxLength(100)),
      "date" -> dateTimeMapping.verifying("Дата должна быть больше текущей", Calendar.getInstance().getTime().before(_)),
      "dscr" -> text.verifying(maxLength(500)))(Event.apply)(Event.unapply)
  }

  def insert = StackAction(AuthorityKey->Permission.editEvent) { implicit request =>
    implicit val user = Some(loggedIn)
    eventForm.bindFromRequest.fold(
      formWithErrors => Ok(views.html.events.insert(formWithErrors)).flashing("error" -> "Исправьте ошибки в форме"),
      success = {
        event =>
          models.Event.insert(event)
          Redirect(routes.Seats.setPrices(event.id)).flashing("success" -> ("Событие добавлено!"))
      })
  }

  def edit(id: Long) = StackAction(AuthorityKey->Permission.editEvent) {
    implicit request =>
      implicit val user = Some(loggedIn)
      val event = Event.getById(id)
      event match {
        case None => NotFound
        case Some(x) => Ok(views.html.events.edit(eventForm.fill(x)))
      }
  }
  def update = StackAction(AuthorityKey->Permission.editEvent) {implicit request =>
    implicit val user = Some(loggedIn)  
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