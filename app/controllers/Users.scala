package controllers

import play.api.mvc.Controller
import play.api.mvc.{ Action, Flash }
import play.api.data.Form
import play.api.i18n._
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import models.User
import jp.t2v.lab.play2.auth.AuthElement
import models.Permission

object Users extends Controller with AuthElement with AuthConfigImpl {
  /*TODO Доработать подтверждение пароля
  val passMap = mapping(
	"pass"->nonEmptyText,
	"passRepeat"->nonEmptyText
  )((pass, passRepeat)=> 
    	if(pass equals passRepeat) Some(pass)
    	else None
    )(pass=>Some("", "")).verifying("Введенные пароли не совпадают!", _ isDefined)
  */
  val userForm = Form {
    mapping(
      "id" -> longNumber,
      "name" -> nonEmptyText,
      "password" -> nonEmptyText,
      "edEv" -> boolean,
      "edS" -> boolean,
      "edU" -> boolean)(User.apply)(User.unapply)
  }

  def newUser = StackAction(AuthorityKey->Permission.editUsers) { implicit request =>
    Ok(views.html.users.addNew(userForm))
  }

  def saveUser = StackAction(AuthorityKey->Permission.editUsers) { implicit request =>
    userForm.bindFromRequest.fold(
      formWithErrors => Ok(views.html.users.addNew(formWithErrors)),
      user => {
        User.addNew(user)
        Redirect(routes.Events.list(false)).flashing("success" -> ("Новый пользователь " + user.name + " успешно создан!"))
      })
  }

  def list = StackAction(AuthorityKey->Permission.editUsers) { implicit request =>
    val users = User.getAll
    Ok(views.html.users.list(users))
  }

  def show (userID:Long) = StackAction(AuthorityKey->Permission.editUsers) { implicit request =>
    User.findByID(userID) match{
      case None=>Redirect(routes.Users.list).flashing("error"->"Пользователь с таким ID не найден")
      case Some(user)=>Ok(views.html.users.details(user))
    }
  }
}