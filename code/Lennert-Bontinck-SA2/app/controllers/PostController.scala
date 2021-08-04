package controllers

import models.comment.{Comment, CommentDao}
import models.like.{Like, LikeDao}
import models.post.{Post, PostDao, PostWithInfoDao}
import models.user.UserDao
import models.visibility.{Visibility, VisibilityDao}
import play.api.data.Form
import play.api.data.Forms.{localDateTime, mapping, nonEmptyText, number}
import play.api.libs.Files
import play.api.mvc._

import java.nio.file.Paths
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * This controller is responsible for handling HTTP requests
 * to the application's post related pages through its actions.
 */
class PostController @Inject()(cc: MessagesControllerComponents,
                               authenticatedUserAction: AuthenticatedUserAction,
                               authenticatedUserActionWithMessageRequest: AuthenticatedUserActionWithMessageRequest,
                               visibilityDao: VisibilityDao,
                               userDao: UserDao,
                               postDao: PostDao,
                               commentDao: CommentDao,
                               postWithInfoDao: PostWithInfoDao,
                               likeDao: LikeDao
                              ) extends MessagesAbstractController(cc) {

  //---------------------------------------------------------------------------
  //| START SHOW POST PAGE RELATED FUNCTIONS
  //---------------------------------------------------------------------------

  /**
   * Create an Action to render the post page for a specific post ID.
   */
  def showPost(id: Int): Action[AnyContent] = authenticatedUserActionWithMessageRequest { implicit request: MessagesRequest[AnyContent] =>
    val username = request.session.get(models.Global.SESSION_USERNAME_KEY).get
    if (!postDao.isValidId(id) || !visibilityDao.userCanViewPost(postDao.findWithId(id), username)) {
      // ID invalid or no rights to view, show index
      Redirect(routes.HomeController.showIndex())
    } else {
      // Get post object
      val postWithInfo = postWithInfoDao.findWithId(id)

      // Display post object
      val username = request.session.get(models.Global.SESSION_USERNAME_KEY).get
      val filledCommentForm = commentForm.fill(Comment(postWithInfo.post.id, username, LocalDateTime.now(), ""))
      Ok(views.html.postPages.post("Post by " + postWithInfo.post.author, postWithInfo, filledCommentForm, commentPostUrl))
    }
  }

  //---------------------------------------------------------------------------
  //| END SHOW POST PAGE RELATED FUNCTIONS
  //---------------------------------------------------------------------------
  //| START LIKE RELATED FUNCTIONS
  //---------------------------------------------------------------------------

  /**
   * Function to process a like attempt, forwards user to post which (s)he liked.
   */
  def processLikeAttempt(post_id: Int): Action[AnyContent] = authenticatedUserAction { implicit request: Request[AnyContent] =>
    // Get username from cookie
    val liker = request.session.get(models.Global.SESSION_USERNAME_KEY).get

    // Check if a valid post id is given
    val valid_id = postDao.isValidId(post_id)

    if (valid_id) {
      // Toggle like
      val like = Like(post_id, liker)
      val now_liked = likeDao.toggleLike(like)

      // Goto post page and show right flash
      if (now_liked) {
        Redirect(routes.PostController.showPost(post_id))
          .flashing("info" -> "You've now liked this post.")
      } else {
        Redirect(routes.PostController.showPost(post_id))
          .flashing("info" -> "Your like from this post was removed.")
      }
    } else {
      // Unexpected form data, perform default action of logging out and showing error.
      Redirect(routes.AuthenticatedUserController
        .logoutWithError("There was something wrong with toggling your like. Please try again after logging in."))
    }
  }


  //---------------------------------------------------------------------------
  //| END LIKE RELATED FUNCTIONS
  //---------------------------------------------------------------------------
  //| START COMMENT RELATED FUNCTIONS
  //---------------------------------------------------------------------------

  /**
   * The comment form and it's verification.
   */
  val commentForm: Form[Comment] = Form(
    mapping(
      "post_id" -> number,
      "author" -> nonEmptyText,
      "date_added" -> localDateTime,
      "text" -> nonEmptyText,
    )(Comment.apply)(Comment.unapply)
  )

  /**
   * The submit URL of the comment form.
   */
  private val commentPostUrl = routes.PostController.processCommentAttempt()

  /**
   * Function to process a comment attempt, forwards user to post on which (s)he commented.
   */
  def processCommentAttempt(): Action[AnyContent] = authenticatedUserActionWithMessageRequest { implicit request: MessagesRequest[AnyContent] =>
    val errorFunction = { formWithErrors: Form[Comment] =>
      // Issues with form itself (validation and/or binding issues).
      // If a valid post ID can be found display error on post page.
      // If no valid POST id is found; unexpected form data, perform default action of logging out and showing error.
      val post_id = formWithErrors.data.getOrElse("post_id", -1).toString.toIntOption.getOrElse(-1)
      val username = request.session.get(models.Global.SESSION_USERNAME_KEY).get
      if (postDao.isValidId(post_id) && visibilityDao.userCanViewPost(postDao.findWithId(post_id), username)) {
        val postWithInfo = postWithInfoDao.findWithId(post_id)
        BadRequest(views.html.postPages.post("Commenting failed", postWithInfo, formWithErrors, commentPostUrl))
      } else {
        Redirect(routes.AuthenticatedUserController
          .logoutWithError("There was something wrong with placing your comment. Please try again after logging in."))
      }

    }
    val successFunction = { comment: Comment =>
      // Form validation and binding is correct, check correct user and existing post
      val correct_user = request.session.get(models.Global.SESSION_USERNAME_KEY).get == comment.author
      val correct_post = postDao.isValidId(comment.post_id)

      if (correct_user && correct_post) {
        // Place comment and show page again!
        commentDao.addComment(comment)
        Redirect(routes.PostController.showPost(comment.post_id))
          .flashing("info" -> "Your comment was added!")
      } else {
        // Unexpected form data, perform default action of logging out and showing error.
        Redirect(routes.AuthenticatedUserController
          .logoutWithError("There was something wrong with placing your comment. Please try again after logging in."))
      }
    }
    val formValidationResult: Form[Comment] = commentForm.bindFromRequest
    formValidationResult.fold(
      errorFunction,
      successFunction
    )
  }


  //---------------------------------------------------------------------------
  //| END COMMENT RELATED FUNCTIONS
  //---------------------------------------------------------------------------
  //| START POST ADDING RELATED FUNCTIONS
  //---------------------------------------------------------------------------

  /**
   * Create an Action to render the add post page.
   */
  def showAddPost(): Action[AnyContent] = authenticatedUserActionWithMessageRequest { implicit request: MessagesRequest[AnyContent] =>
    val username = request.session.get(models.Global.SESSION_USERNAME_KEY).get
    val filledAddPostForm = addPostForm.fill(Post(-1, username, LocalDateTime.now(), "", "TempFileName"))
    val allUsernames = userDao.findAllOtherUsers(username)
    Ok(views.html.postPages.addPost("Add post", filledAddPostForm, addPostUrl, allUsernames))
  }

  /**
   * The user add post form and it's verification.
   */
  val addPostForm: Form[Post] = Form(
    mapping(
      "post_id" -> number,
      "author" -> nonEmptyText,
      "date_added" -> localDateTime,
      "description" -> nonEmptyText,
      "image_filename" -> nonEmptyText,
    )(Post.apply)(Post.unapply)
  )

  /**
   * The submit URL of the add post form.
   */
  private val addPostUrl = routes.PostController.processAddPostAttempt()

  /**
   * Function to process a comment attempt, forwards user to post on which (s)he commented.
   */
  def processAddPostAttempt(): Action[MultipartFormData[play.api.libs.Files.TemporaryFile]] = authenticatedUserActionWithMessageRequest(parse.multipartFormData) { implicit request: MessagesRequest[MultipartFormData[Files.TemporaryFile]] =>
    val errorFunction = { formWithErrors: Form[Post] =>
      // Issues with form itself (validation and/or binding issues).
      val username = request.session.get(models.Global.SESSION_USERNAME_KEY).get
      val allUsernames = userDao.findAllOtherUsers(username)
      BadRequest(views.html.postPages.addPost("Adding post failed", formWithErrors, addPostUrl, allUsernames))
    }
    val successFunction = { post: Post =>
      // Form validation and binding is correct, check correct user
      val correct_user = request.session.get(models.Global.SESSION_USERNAME_KEY).get == post.author

      if (correct_user) {
        // Check if file is provided
        // As per https://www.playframework.com/documentation/2.8.x/ScalaFileUpload
        val files: Option[MultipartFormData.FilePart[Files.TemporaryFile]] = request.body.file("image")
        files.map { image =>
          // Check for right extension
          val extension = image.filename.split("\\.").last

          if (extension == "jpeg" || extension == "jpg") {

            // Check for correct visibility settings
            val visibility_settings_ok = request.body.dataParts.contains("visibility")

            if (visibility_settings_ok) {
              // Create unique file name
              val username = request.session.get(models.Global.SESSION_USERNAME_KEY).get
              val filename = username + "-" + System.currentTimeMillis().toString + "." + extension

              // Copy file to right folder
              image.ref.copyTo(Paths.get(s"public/images/posts/$filename"), replace = true)

              // Wait for some time so that copy is performed
              // It seems like there is no "onComplete" like procedures possible on the copyTo function.
              Thread.sleep(1000)

              // Create right post object to add
              val post_to_add = Post(post.id, username, LocalDateTime.now(), post.description, filename)

              // Create right visibility object
              val visible_to_all = request.body.dataParts("visibility").contains("all")
              val visible_to_users = request.body.dataParts.getOrElse("shareWithUsernames", List()).toList
              val visibility = Visibility(post.id, visible_to_all = visible_to_all, visible_to_users)

              // Add the post and retrieve its id
              val post_id = postDao.addPost(post_to_add, visibility)

              // Display post
              Redirect(routes.PostController.showPost(post_id))
                .flashing("info" -> "Your post was created. It might take some time for your image to display, try refreshing if you can't see it.")
            } else {
              // visibility not okay
              val username = request.session.get(models.Global.SESSION_USERNAME_KEY).get
              val filledAddPostForm = addPostForm.fill(Post(-1, username, LocalDateTime.now(), post.description, "WrongVisibility"))
              val allUsernames = userDao.findAllOtherUsers(username)
              BadRequest(views.html.postPages.addPost("Wrong visibility - add post", filledAddPostForm, addPostUrl, allUsernames, issueWithVisibility = true))
            }

          } else {
            // File extension not okay
            val username = request.session.get(models.Global.SESSION_USERNAME_KEY).get
            val filledAddPostForm = addPostForm.fill(Post(-1, username, LocalDateTime.now(), post.description, "WrongFileExtension"))
            val allUsernames = userDao.findAllOtherUsers(username)
            BadRequest(views.html.postPages.addPost("Wrong file - add post", filledAddPostForm, addPostUrl, allUsernames, issueWithFile = true))
          }


        }.getOrElse {
          // File was not provided
          val username = request.session.get(models.Global.SESSION_USERNAME_KEY).get
          val filledAddPostForm = addPostForm.fill(Post(-1, username, LocalDateTime.now(), post.description, "NonProvidedFile"))
          val allUsernames = userDao.findAllOtherUsers(username)
          BadRequest(views.html.postPages.addPost("Missing file - add post", filledAddPostForm, addPostUrl, allUsernames, issueWithFile = true))
        }

      } else {
        // Unexpected form data, perform default action of logging out and showing error.
        Redirect(routes.AuthenticatedUserController
          .logoutWithError("There was something wrong with adding your post. Please try again after logging in."))
      }
    }
    val formValidationResult: Form[Post] = addPostForm.bindFromRequest
    formValidationResult.fold(
      errorFunction,
      successFunction
    )
  }
  //---------------------------------------------------------------------------
  //| END POST ADDING RELATED FUNCTIONS
  //---------------------------------------------------------------------------
  //| START POST DELETE RELATED FUNCTIONS
  //---------------------------------------------------------------------------

  /**
   * Function to process a like attempt, forwards user to post which (s)he liked.
   */
  def processPostDeleteAttempt(post_id: Int): Action[AnyContent] = authenticatedUserAction { implicit request: Request[AnyContent] =>
    // Get username from cookie
    val deleter = request.session.get(models.Global.SESSION_USERNAME_KEY).get

    // Check if valid deleter is author of post
    val valid_author = postDao.isAuthor(post_id, deleter)

    if (valid_author) {
      // Get post and delete it
      val post = postDao.findWithId(post_id)
      postDao.deletePost(post)

      // Go to profile page after deletion
      Redirect(routes.UserController.showProfile(deleter))
        .flashing("info" -> "Your post was deleted")
    } else {
      // Unexpected form data, perform default action of logging out and showing error.
      Redirect(routes.AuthenticatedUserController
        .logoutWithError("There was something wrong with deleting your post. Please try again after logging in."))
    }
  }


  //---------------------------------------------------------------------------
  //| END POST DELETE RELATED FUNCTIONS
  //---------------------------------------------------------------------------
  //| START EDIT VISIBILITY RELATED FUNCTIONS
  //---------------------------------------------------------------------------


  /**
   * Create an Action to render the add post page.
   */
  def showEditVisibility(id: Int): Action[AnyContent] = authenticatedUserActionWithMessageRequest { implicit request: MessagesRequest[AnyContent] =>
    val username = request.session.get(models.Global.SESSION_USERNAME_KEY).get

    if (postDao.isAuthor(id, username)) {
      val allUsernames = userDao.findAllOtherUsers(username)
      val postWithInfo = postWithInfoDao.findWithId(id)
      Ok(views.html.postPages.editVisibility("Edit visibility of post", postWithInfo, editPostVisibilityUrl, allUsernames))
    } else {
      // Unexpected form data, perform default action of logging out and showing error.
      Redirect(routes.AuthenticatedUserController
        .logoutWithError("There was something wrong with editing your post. Please try again after logging in."))
    }
  }

  /**
   * The submit URL of the edit post visibility form.
   */
  //todo
  private val editPostVisibilityUrl = routes.PostController.processEditVisibilityAttempt()

  def processEditVisibilityAttempt(): Action[MultipartFormData[play.api.libs.Files.TemporaryFile]] = authenticatedUserActionWithMessageRequest(parse.multipartFormData) { implicit request: MessagesRequest[MultipartFormData[Files.TemporaryFile]] =>
    val visibility_settings_ok = request.body.dataParts.contains("visibility") && request.body.dataParts.contains("post_id")

    if (visibility_settings_ok) {
      // Check if valid deleter is author of post
      val edit_author = request.session.get(models.Global.SESSION_USERNAME_KEY).get
      val post_id = request.body.dataParts("post_id").toList.head.toInt
      val valid_author = postDao.isAuthor(post_id, edit_author)

      if (valid_author) {
        // Make new visibility object
        val visible_to_all = request.body.dataParts("visibility").contains("all")
        val visible_to_users = request.body.dataParts.getOrElse("shareWithUsernames", List()).toList
        val newVisibilities = Visibility(post_id, visible_to_all, visible_to_users)
        val post = postDao.findWithId(post_id)

        // Edit visibility
        visibilityDao.editVisibility(post, newVisibilities)

        // Show post
        Redirect(routes.PostController.showPost(post_id))
          .flashing("info" -> "Visibility has been changed!")
      } else {
        // Unexpected form data, perform default action of logging out and showing error.
        Redirect(routes.AuthenticatedUserController
          .logoutWithError("There was something wrong with deleting your post. Please try again after logging in."))
      }
    } else {
      // Unexpected form data, perform default action of logging out and showing error.
      Redirect(routes.AuthenticatedUserController
        .logoutWithError("There was something wrong with deleting your post. Please try again after logging in."))
    }
  }



  //---------------------------------------------------------------------------
  //| END EDIT VISIBILITY RELATED FUNCTIONS
  //---------------------------------------------------------------------------


}
