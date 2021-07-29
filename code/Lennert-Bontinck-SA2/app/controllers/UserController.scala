package controllers

import javax.inject.Inject
import models.{Global, User, UserDao}
import play.api.data.Forms._
import play.api.data._
import play.api.mvc._

/**
 * This controller is responsible for handling HTTP requests
 * to the application's user related pages through its actions.
 */
class UserController @Inject()(
                                cc: MessagesControllerComponents,
                                userDao: UserDao
                              ) extends MessagesAbstractController(cc) {

  //private val logger = play.api.Logger(this.getClass)

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
   * The user register form and it's verification.
   * Checks if username and password are of correct length.
   * Checks if password is complicated enough.
   */
  val registerForm: Form[User] = Form(
    mapping(
      "username" -> nonEmptyText
        .verifying("Username must be between 3 and 15 characters.", username => correctUsernameLength(username))
        .verifying("Username can only contain letters and numbers", username => lettersAndNumbersOnly(username))
        .verifying("Username already taken.", username => userDao.uniqueUsername(username)),
      "password" -> nonEmptyText
        .verifying("Password must be between 5 and 20 characters.", password => correctPasswordLength(password))
        .verifying("Password can only contain letters and numbers", username => lettersAndNumbersOnly(username)),
    )(User.apply)(User.unapply)
  )

  /**
   * The submit URL of the user login form.
   */
  private val loginFormSubmitUrl = routes.UserController.processLoginAttempt()

  /**
   * The submit URL of the user register form.
   */
  private val registerFormSubmitUrl = routes.UserController.processRegisterAttempt()

  /**
   * Create an Action to render the login HTML page.
   * If the user is already logged in he's forwarded to index.
   */
  def showLogin: Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    if (request.session.get(models.Global.SESSION_USERNAME_KEY).isEmpty) {
      Ok(views.html.login("Login", loginForm, loginFormSubmitUrl))
    } else {
      Redirect(routes.HomeController.showIndex())
    }
  }

  /**
   * Create an Action to render the register HTML page.
   * If the user is already logged in he's forwarded to index.
   */
  def showRegister: Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    if (request.session.get(models.Global.SESSION_USERNAME_KEY).isEmpty) {
      Ok(views.html.register("Register", registerForm, registerFormSubmitUrl))
    } else {
      Redirect(routes.HomeController.showIndex())
    }
  }

  /**
   * Function to process a login attempt, if credentials are correct the user is forwarded to index
   * otherwise the login page is shown again.
   */
  def processLoginAttempt: Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    val errorFunction = { formWithErrors: Form[User] =>
      // Issues with form itself (validation and/or binding issues)
      BadRequest(views.html.login("Login unsuccessful", formWithErrors, loginFormSubmitUrl))
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

  /**
   * Function to process a register attempt, if user creation is possible the user is forwarded to index
   * otherwise the register page is shown again.
   */
  def processRegisterAttempt: Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    val errorFunction = { formWithErrors: Form[User] =>
      // Issues with form itself (validation and/or binding issues)
      BadRequest(views.html.register("Registration failed", formWithErrors, registerFormSubmitUrl))
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

  private def correctUsernameLength(username: String): Boolean = {
    username.length >= 3 && username.length <= 15
  }

  private def lettersAndNumbersOnly(string: String): Boolean = {
    string.matches("[a-zA-Z0-9]*")
  }

  private def correctPasswordLength(password: String): Boolean = {
    password.length >= 5 && password.length <= 20
  }

}
