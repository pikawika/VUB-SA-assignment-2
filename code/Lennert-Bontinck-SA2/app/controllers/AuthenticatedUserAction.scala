package controllers

import javax.inject.Inject
import play.api.mvc.Results._
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

/**
 * NOTE: This file is taken from the solutions of WPO session 7.
 * The only noteworthy modification is a forward to showLoginRequired instead of index.
 */
class AuthenticatedUserAction @Inject()(parser: BodyParsers.Default)(implicit ec: ExecutionContext)
  extends ActionBuilderImpl(parser) {

  override def invokeBlock[A](request: Request[A], block: Request[A] => Future[Result]): Future[Result] = {
    val maybeUsername = request.session.get(models.Global.SESSION_USERNAME_KEY)
    maybeUsername match {
      case None =>
        // If user is not logged in show login required page.
        Future.successful(Redirect(routes.HomeController.showLoginRequired()))
      case Some(_) =>
        val res: Future[Result] = block(request)
        res
    }
  }
}
