package controllers

import play.api.mvc.Controller
import play.api.mvc.Action
import models.Event
import java.util.Date
import play.api.data.Form
import play.api.data.Forms._

object Events extends Controller {
	def list = Action{
	  implicit request=>
	    val events=Event.getAll
	    //TODO добавить ссылку на темплейт со списком
	    NotImplemented
	}
	def add=Action{
	  implicit request =>
	  //TODO заменить на ссылку на форму продукта
	  Redirect(routes.Events.list())
	}
	
	val eventMap=mapping(
		"id"->longNumber,
	    "tp"->text,
		"name"->text,
		"date"->date,
		"dscr"->text
	)(Event.apply)(Event.unapply)
	val eventForm=Form(eventMap)
	def processForm=Action{implicit request=>
		eventForm.bindFromRequest.fold(
			hasErrors=(form=>Redirect(routes.Events.add())),
			success={
			  event=>models.Event.insert(event)
			  Redirect(routes.Events.list())
			}
		)
	}
}