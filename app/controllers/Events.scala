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
	    NotImplemented
	}
	def add=Action{
	  implicit request =>
	  	val event=Event(0,"match","Черноморец-Барселона",new Date(2014,6,18),"cool")
	  	Event.insert(event)
	  	Redirect(routes.Events.list())
	}
}