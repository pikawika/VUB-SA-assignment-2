package controllers

import javax.inject._
import play.api.mvc._

/**
 * This controller is responsible for handling HTTP requests
 * to the application's main content pages through its actions.
 */
@Singleton
class HomeController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  /**
   * Create an Action to render the index HTML page.
   * If the user is not logged in (s)he will be forwarded to the login required HTML page.
   */
  def showIndex() = Action { implicit request: Request[AnyContent] =>

    if (request.session.get(models.Global.SESSION_USERNAME_KEY).isEmpty) {
      Ok(views.html.loginRequired("Content hidden"))
    } else {
      Ok(views.html.index("Home"))
    }
  }

}
