package controllers

import models.user.{User, UserDao}

import javax.inject.Inject
import models.Global
import models.post.PostWithInfoDao
import play.api.data.Forms._
import play.api.data._
import play.api.mvc._

/**
 * This controller is responsible for handling HTTP requests
 * to the application's user related pages through its actions.
 * NOTE: The login portion of this file is heavily inspired on the solutions of WPO session 7.
 */
class UserController @Inject()(cc: MessagesControllerComponents,
                               authenticatedUserAction: AuthenticatedUserAction,
                               postWithInfoDao: PostWithInfoDao,
                               userDao: UserDao
                              ) extends MessagesAbstractController(cc) {


  //---------------------------------------------------------------------------
  //| START LOGIN RELATED FUNCTIONS
  //---------------------------------------------------------------------------
  /**
   * The user login form and it's verification.
   * Only checks for non-empty text.
   */
  val loginForm: Form[User] = Form(
    mapping(
      "username" -> nonEmptyText,
      "password" -> nonEmptyText,
    )(User.apply)(User.unapply)
  )


  /**
   * The submit URL of the user login form.
   */
  private val loginFormSubmitUrl = routes.UserController.processLoginAttempt()


  /**
   * Create an Action to render the login HTML page.
   * If the user is already logged in he's forwarded to index.
   */
  def showLogin: Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    if (request.session.get(models.Global.SESSION_USERNAME_KEY).isEmpty) {
      Ok(views.html.userPages.login("Login", loginForm, loginFormSubmitUrl))
    } else {
      Redirect(routes.HomeController.showIndex())
    }
  }

  /**
   * Function to process a login attempt, if login succeeded the user is forwarded to index
   * otherwise the login page is shown again.
   */
  def processLoginAttempt: Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    val errorFunction = { formWithErrors: Form[User] =>
      // Issues with form itself (validation and/or binding issues)
      BadRequest(views.html.userPages.login("Login unsuccessful", formWithErrors, loginFormSubmitUrl))
    }
    val successFunction = { user: User =>
      // Form validation and binding is correct, check if user exist.
      val foundUser: Boolean = userDao.authenticateUser(user)
      if (foundUser) {
        // User OK, show index (which will now be post overview).
        Redirect(routes.HomeController.showIndex())
          .withSession(Global.SESSION_USERNAME_KEY -> user.username)
      } else {
        // User not found, show login with error.
        Redirect(routes.UserController.showLogin())
          .flashing("error" -> "Invalid username/password.")
      }
    }
    val formValidationResult: Form[User] = loginForm.bindFromRequest
    formValidationResult.fold(
      errorFunction,
      successFunction
    )
  }

  //---------------------------------------------------------------------------
  //| END LOGIN RELATED FUNCTIONS
  //---------------------------------------------------------------------------
  //| START REGISTER RELATED FUNCTIONS
  //---------------------------------------------------------------------------

  /**
   * The user register form and it's verification.
   * Checks if username and password are of correct length and format.
   * Checks if username is not already taken.
   */
  val registerForm: Form[User] = Form(
    mapping(
      "username" -> nonEmptyText
        .verifying("Username must be between 3 and 15 characters.", username => lengthBetween(username, 3, 15))
        .verifying("Username can only contain letters and numbers", username => lettersAndNumbersOnly(username))
        .verifying("Username already taken.", username => userDao.uniqueUsername(username)),
      "password" -> nonEmptyText
        .verifying("Password must be between 5 and 20 characters.", password => lengthBetween(password, 5, 20))
        .verifying("Password can only contain letters and numbers", username => lettersAndNumbersOnly(username)),
    )(User.apply)(User.unapply)
  )

  /**
   * The submit URL of the user register form.
   */
  private val registerFormSubmitUrl = routes.UserController.processRegisterAttempt()

  /**
   * Create an Action to render the register HTML page.
   * If the user is already logged in he's forwarded to index.
   */
  def showRegister: Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    if (request.session.get(models.Global.SESSION_USERNAME_KEY).isEmpty) {
      Ok(views.html.userPages.register("Register", registerForm, registerFormSubmitUrl))
    } else {
      Redirect(routes.HomeController.showIndex())
    }
  }

  /**
   * Function to process a register attempt, if registration succeeded the user is forwarded to index
   * otherwise the register page is shown again.
   */
  def processRegisterAttempt: Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    val errorFunction = { formWithErrors: Form[User] =>
      // Issues with form itself (validation and/or binding issues)
      BadRequest(views.html.userPages.register("Registration failed", formWithErrors, registerFormSubmitUrl))
    }
    val successFunction = { user: User =>
      // Form validation and binding is correct, try to make user
      val userAdded: Boolean = userDao.addUser(user)
      if (userAdded) {
        // User creation succeeded, forward user to login screen
        Redirect(routes.UserController.showLogin())
          .flashing("info" -> "Account creation succeeded, you can now login.")
      } else {
        // User creation failed, show register with error.
        Redirect(routes.UserController.showLogin())
          .flashing("error" -> "Account creation failed.")
      }
    }
    val formValidationResult: Form[User] = registerForm.bindFromRequest
    formValidationResult.fold(
      errorFunction,
      successFunction
    )
  }

  //---------------------------------------------------------------------------
  //| END REGISTER RELATED FUNCTIONS
  //---------------------------------------------------------------------------
  //| START VALIDATION RELATED FUNCTIONS
  //---------------------------------------------------------------------------

  /**
   * Function to validate if the length of a string is between a min and max value.
   */
  private def lengthBetween(username: String, min: Int, max: Int): Boolean = {
    username.length >= min && username.length <= max
  }

  /**
   * Function to validate use of letters and numbers only in a string.
   */
  private def lettersAndNumbersOnly(string: String): Boolean = {
    string.matches("[a-zA-Z0-9]*")
  }

  //---------------------------------------------------------------------------
  //| END VALIDATION RELATED FUNCTIONS
  //---------------------------------------------------------------------------
  //| START PROFILE RELATED FUNCTIONS
  //---------------------------------------------------------------------------

  /**
   * Create an Action to render the user profile HTML page.
   * Posts of user are sorted on newest first.
   * Only accessible to logged in users.
   */
  def showProfile(username: String): Action[AnyContent] = authenticatedUserAction { implicit request: Request[AnyContent] =>
    val posts = postWithInfoDao.findFromUser(username)
    Ok(views.html.userPages.userPostOverview("Posts by " + username, posts, username))
  }


  //---------------------------------------------------------------------------
  //| END PROFILE RELATED FUNCTIONS
  //---------------------------------------------------------------------------
}
