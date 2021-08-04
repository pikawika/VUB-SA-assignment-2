package controllers

import javax.inject._
import play.api.mvc._

/**
 * NOTE: This file is taken from the solutions of WPO session 7.
 * Noteworthy change: function was added to logout and flash an error message
 */
@Singleton
class AuthenticatedUserController @Inject()(cc: ControllerComponents,
                                            authenticatedUserAction: AuthenticatedUserAction) extends AbstractController(cc) {

  /**
   * Clears sessions thus logging out current user.
   */
  def logout: Action[AnyContent] = authenticatedUserAction { implicit request: Request[AnyContent] =>
    // docs: “withNewSession ‘discards the whole (old) session’”
    Redirect(routes.UserController.showLogin())
      .flashing("info" -> "You are logged out.")
      .withNewSession
  }

  /**
   * Clears sessions thus logging out current user and shows provided error message.
   */
  def logoutWithError(message: String): Action[AnyContent] = authenticatedUserAction { implicit request: Request[AnyContent] =>
    // docs: “withNewSession ‘discards the whole (old) session’”
    Redirect(routes.UserController.showLogin())
      .flashing("error" -> message)
      .withNewSession
  }

}

