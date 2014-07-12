package controllers

import play.api.mvc.Controller
import play.api.mvc.{ Action, Flash }
import models.User
import jp.t2v.lab.play2.auth.AuthElement
import models.Permission

object General extends Controller with AuthElement with AuthConfigImpl {

  def logged = StackAction(AuthorityKey -> Permission.anyUser) { implicit request =>
    val user = loggedIn
    if (user.isNew)
      Redirect(routes.Users.changePassword).flashing("info" -> "Перед первым входом в систему Вы должны сменить пароль")
    else {
      if (!user.isActive)
        Redirect(routes.Application.login).flashing("error" -> "Данный пользователь заблокирован администратором")
      else
        Redirect(routes.Events.list(false))
    }
  }
}