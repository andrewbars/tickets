package controllers

import play.api._
import play.api.mvc._
import jp.t2v.lab.play2.auth.LoginLogout
import models.User
import play.api.mvc.{ Action, Flash }
import play.api.data.Form
import play.api.i18n._
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import jp.t2v.lab.play2.auth.OptionalAuthElement

object Application extends Controller with AuthConfigImpl with LoginLogout with OptionalAuthElement {

  def index = Action {
    Redirect(routes.Application.login)
  }

  val loginForm = Form {
    mapping("login" -> text, "password" -> text)(User.auth)(_.map(u=>(u.name, "")))
    .verifying("Введены неправильный логин или пароль", user => user.isDefined)
  }

  def login = StackAction { implicit request =>
    loggedIn match{
      case None=>Ok(views.html.login(loginForm))
      case Some(user)=>Redirect(routes.Events.list)
    }
    
  }

  def logout = Action.async { implicit request =>
    gotoLogoutSucceeded
  }
  
  def authenticate = Action.async{implicit request=>
  	loginForm.bindFromRequest.fold(
  		formWithErrors=>Future.successful(Ok(views.html.login(formWithErrors))),
  		user =>gotoLoginSucceeded(user.get.id)
  	)
  }

}