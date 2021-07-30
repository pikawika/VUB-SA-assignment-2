package controllers

import models.post.PostWithInfoDao

import javax.inject._
import play.api.mvc._

/**
 * This controller is responsible for handling HTTP requests
 * to the application's "home page" through its actions.
 */
@Singleton
class HomeController @Inject()(cc: ControllerComponents,
                               authenticatedUserAction: AuthenticatedUserAction,
                               postWithInfoDao: PostWithInfoDao) extends AbstractController(cc) {

  //---------------------------------------------------------------------------
  //| START INDEX RELATED FUNCTIONS
  //---------------------------------------------------------------------------
  /**
   * Create an Action to render the index HTML page.
   * Only accessible to logged in users.
   */
  def showIndex(): Action[AnyContent] = authenticatedUserAction { implicit request: Request[AnyContent] =>
    val posts = postWithInfoDao.findAllWithFewComments
    Ok(views.html.posts.postOverview("Home", posts))
  }

  //---------------------------------------------------------------------------
  //| END INDEX RELATED FUNCTIONS
  //---------------------------------------------------------------------------
  //| START LOGIN REQUIRED RELATED FUNCTIONS
  //---------------------------------------------------------------------------

  /**
   * Create an Action to render the login required HTML page.
   * If user is logged in he's forwarded to the home page.
   */
  def showLoginRequired(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    if (request.session.get(models.Global.SESSION_USERNAME_KEY).isEmpty) {
      Ok(views.html.userPages.loginRequired("Content hidden"))
    } else {
      Redirect(routes.HomeController.showIndex())
    }
  }

  //---------------------------------------------------------------------------
  //| END LOGIN REQUIRED RELATED FUNCTIONS
  //---------------------------------------------------------------------------
}
