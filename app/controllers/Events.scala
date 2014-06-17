package controllers

import play.api.mvc.Controller
import play.api.mvc.Action
import models.Event
import java.util.Date

object Events extends Controller {
	def list = Action{
	  implicit request=>
	    val events=Event.getAll
	    //TODO добавить ссылку на темплейт со списком
	    Ok(views.html.events.list(events))
	}
	def show(id:Long)=Action{
	  implicit request=>
	    val e=Event.getById(id)
	    NotImplemented
	}
	def add=Action{
	  implicit request =>
	  Redirect(routes.Events.list())
	}
}