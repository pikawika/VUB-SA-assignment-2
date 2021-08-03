package controllers

import play.api.i18n.MessagesApi
import play.api.mvc.Results._
import play.api.mvc._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

/**
 * NOTE: This file is a modification of AuthenticatedUserAction as taken from the solutions of WPO session 7.
 * It allows for restricting MessagesAction to registered users only, handy places where AuthenticatedUserAction isn't usable.
 */

class AuthenticatedUserActionWithMessageRequest @Inject()(parser: BodyParsers.Default,  messagesApi: MessagesApi)(implicit ec: ExecutionContext)
  extends MessagesActionBuilderImpl(parser, messagesApi) {

  override def invokeBlock[A](request: Request[A], block: MessagesRequest[A] => Future[Result]): Future[Result] = {
    val maybeUsername = request.session.get(models.Global.SESSION_USERNAME_KEY)
    maybeUsername match {
      case None =>
        // If user is not logged in show login required page.
        Future.successful(Redirect(routes.HomeController.showLoginRequired()))
      case Some(_) =>
        // Create MessagesRequest from request and API
        val res: Future[Result] = block(new MessagesRequest[A](request, messagesApi))
        res
    }
  }
}
