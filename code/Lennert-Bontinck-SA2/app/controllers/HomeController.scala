package controllers

import models.PostDao

import javax.inject._
import play.api.mvc._

/**
 * This controller is responsible for handling HTTP requests
 * to the application's main content pages through its actions.
 */
@Singleton
class HomeController @Inject()(cc: ControllerComponents,
                               authenticatedUserAction: AuthenticatedUserAction,
                               postDao: PostDao) extends AbstractController(cc) {

  /**
   * Create an Action to render the index HTML page.
   * Only accessible to logged in users.
   */
  def showIndex = authenticatedUserAction { implicit request: Request[AnyContent] =>
    val posts = postDao.findAll
    Ok(views.html.postOverview("Home", posts))
  }

  /**
   * Create an Action to render the login required HTML page.
   * If user is logged in he's forwarded to the home page.
   */
  def showLoginRequired() = Action { implicit request: Request[AnyContent] =>

    if (request.session.get(models.Global.SESSION_USERNAME_KEY).isEmpty) {
      Ok(views.html.loginRequired("Content hidden"))
    } else {
      Redirect(routes.HomeController.showIndex())
    }
  }
}
