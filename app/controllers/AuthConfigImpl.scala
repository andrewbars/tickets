package controllers

import jp.t2v.lab.play2.auth.AuthConfig
import models._
import scala.concurrent.ExecutionContext
import scala.reflect.ClassTag
import scala.concurrent.Future
import play.api.mvc._
import play.api.mvc.Results._

trait AuthConfigImpl extends AuthConfig {
  type Id = Long

  type User = models.User

  type Authority = User => Boolean

  val idTag: ClassTag[Id] = ClassTag.Long

  val sessionTimeoutInSeconds: Int = 300

  def resolveUser(id: Id)(implicit ctx: ExecutionContext): Future[Option[User]] = Future(models.User.findByID(id))

  def loginSucceeded(request: RequestHeader)(implicit ctx: ExecutionContext): Future[SimpleResult] =
    Future.successful(Redirect(routes.General.logged))
    
  def logoutSucceeded(request: RequestHeader)(implicit ctx: ExecutionContext): Future[SimpleResult] =
    Future.successful(Redirect(routes.Application.login))


  def authenticationFailed(request: RequestHeader)(implicit ctx: ExecutionContext): Future[SimpleResult] =
    Future.successful(Redirect(routes.Application.login).flashing("error"->"Для доступа к данной странице необходимо войти в систему!"))


  def authorizationFailed(request: RequestHeader)(implicit ctx: ExecutionContext): Future[SimpleResult] = 
    Future.successful(Forbidden("no permission"))

  def authorize(user: User, authority: Authority)(implicit ctx: ExecutionContext): Future[Boolean] = Future.successful {
	  authority(user)
  }
  override lazy val cookieSecureOption: Boolean = play.api.Play.current.configuration.getBoolean("auth.cookie.secure").getOrElse(false)
}