package controllers

import javax.inject._
import play.api.mvc._
@Singleton
class LandingPageController @Inject()(
    cc: ControllerComponents,
    authenticatedUserAction: AuthenticatedUserAction
) extends AbstractController(cc) {

    // this is where the user comes immediately after logging in.
    // notice that this uses `authenticatedUserAction`.
    def showLandingPage() = authenticatedUserAction { implicit request: Request[AnyContent] =>
        Ok(views.html.loginLandingPage())
    }

}

