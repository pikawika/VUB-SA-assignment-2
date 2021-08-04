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
                               likeDao: LikeDao) extends MessagesAbstractController(cc) {

  //---------------------------------------------------------------------------
  //| START SHOW POST PAGE RELATED FUNCTIONS
  //---------------------------------------------------------------------------

  /**
   * Create an Action to render the post page for a specific post ID.
   * Only accessible to logged in users with rights to view post.
   */
  def showPost(postId: Int): Action[AnyContent] = authenticatedUserActionWithMessageRequest { implicit request: MessagesRequest[AnyContent] =>
    val username = request.session.get(models.Global.SESSION_USERNAME_KEY).get
    if (!postDao.isValidId(postId) || !visibilityDao.userCanViewPost(postDao.findWithId(postId), username)) {
      // ID invalid or no rights to view, show index
      Redirect(routes.HomeController.showIndex())
        .flashing("info" -> "The post you requested is not available to you")
    } else {
      // Get post object
      val postWithInfo = postWithInfoDao.findWithId(postId)

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
   * Only accessible to logged in users with rights to view post.
   */
  def processLikeAttempt(postId: Int): Action[AnyContent] = authenticatedUserAction { implicit request: Request[AnyContent] =>
    // Get username from cookie
    val liker = request.session.get(models.Global.SESSION_USERNAME_KEY).get

    if (postDao.isValidId(postId) && visibilityDao.userCanViewPost(postDao.findWithId(postId), liker)) {
      // Toggle like
      val like = Like(postId, liker)
      val userHasNowLiked = likeDao.toggleLike(like)

      // Goto post page and show right flash
      if (userHasNowLiked) {
        Redirect(routes.PostController.showPost(postId))
          .flashing("info" -> "You've now liked this post.")
      } else {
        Redirect(routes.PostController.showPost(postId))
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
   * Function to process a comment attempt, forwards user to post on which (s)he commented on success.
   * Only accessible to logged in users with rights to view post.
   */
  def processCommentAttempt(): Action[AnyContent] = authenticatedUserActionWithMessageRequest { implicit request: MessagesRequest[AnyContent] =>
    val errorFunction = { formWithErrors: Form[Comment] =>
      // Issues with form itself (validation and/or binding issues).
      // If a valid post ID can be found display error on post page.
      // If no valid POST id is found; unexpected form data, perform default action of logging out and showing error.
      val postId = formWithErrors.data.getOrElse("post_id", -1).toString.toIntOption.getOrElse(-1)
      val username = request.session.get(models.Global.SESSION_USERNAME_KEY).get
      if (postDao.isValidId(postId) && visibilityDao.userCanViewPost(postDao.findWithId(postId), username)) {
        val postWithInfo = postWithInfoDao.findWithId(postId)
        BadRequest(views.html.postPages.post("Commenting failed", postWithInfo, formWithErrors, commentPostUrl))
      } else {
        Redirect(routes.AuthenticatedUserController
          .logoutWithError("There was something wrong with placing your comment. Please try again after logging in."))
      }

    }
    val successFunction = { comment: Comment =>
      // Form validation and binding is correct, check correct user and existing post with view access
      val username = request.session.get(models.Global.SESSION_USERNAME_KEY).get
      val userIsAuthor = username == comment.author
      val isCorrectPost = postDao.isValidId(comment.postId) && visibilityDao.userCanViewPost(postDao.findWithId(comment.postId), username)

      if (userIsAuthor && isCorrectPost) {
        // Place comment and show page again!
        commentDao.addComment(comment)
        Redirect(routes.PostController.showPost(comment.postId))
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
   * Only accessible to logged in users.
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
   * Function to process an add post attempt, forwards user to post (s)he added
   * Only accessible to logged in users.
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
      val correctUser = request.session.get(models.Global.SESSION_USERNAME_KEY).get == post.author
      if (correctUser) {
        // Check if file is provided
        // As per akka play doc: https://www.playframework.com/documentation/2.8.x/ScalaFileUpload
        val files: Option[MultipartFormData.FilePart[Files.TemporaryFile]] = request.body.file("image")
        files.map { image =>
          // Check for right extension
          val extension = image.filename.split("\\.").last
          if (extension == "jpeg" || extension == "jpg") {
            // Check for correct visibility settings of form
            val correctVisibilitySettingsSupplied = request.body.dataParts.contains("visibility")
            if (correctVisibilitySettingsSupplied) {
              // Create unique file name
              val username = request.session.get(models.Global.SESSION_USERNAME_KEY).get
              val filename = username + "-" + System.currentTimeMillis().toString + "." + extension

              // Copy file to right folder
              image.ref.copyTo(Paths.get(s"public/images/posts/$filename"), replace = true)

              // Wait for some time so that copy is performed
              // NOTE: It seems like there is no "onComplete" like procedures possible on the copyTo function.
              Thread.sleep(1000)

              // Create right post object to add
              val postToAdd = Post(post.id, username, LocalDateTime.now(), post.description, filename)

              // Create right visibility object
              val postVisibleToAll = request.body.dataParts("visibility").contains("all")
              val listOfUsersPostVisibleTo = request.body.dataParts.getOrElse("shareWithUsernames", List()).toList
              val visibility = Visibility(post.id, isVisibleToEveryone = postVisibleToAll, listOfUsersPostVisibleTo)

              // Add the post and retrieve its id
              val newPostId = postDao.addPost(postToAdd, visibility)

              // Display newly added post
              Redirect(routes.PostController.showPost(newPostId))
                .flashing("info" -> "Your post was created. It might take some time for your image to display, try refreshing if you can't see it.")
            } else {
              // check for visibility settings of form else: visibility not okay
              val username = request.session.get(models.Global.SESSION_USERNAME_KEY).get
              val filledAddPostForm = addPostForm.fill(Post(-1, username, LocalDateTime.now(), post.description, "WrongVisibility"))
              val allUsernames = userDao.findAllOtherUsers(username)
              BadRequest(views.html.postPages.addPost("Wrong visibility - add post", filledAddPostForm, addPostUrl, allUsernames, issueWithVisibility = true))
            }

          } else {
            // File extension else: file extension not okay
            val username = request.session.get(models.Global.SESSION_USERNAME_KEY).get
            val filledAddPostForm = addPostForm.fill(Post(-1, username, LocalDateTime.now(), post.description, "WrongFileExtension"))
            val allUsernames = userDao.findAllOtherUsers(username)
            BadRequest(views.html.postPages.addPost("Wrong file - add post", filledAddPostForm, addPostUrl, allUsernames, issueWithFile = true))
          }

        }.getOrElse {
          // Map for file else: File was not provided
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
   * Function to process a post delete attempt, forwards user to their profile on successful deletion.
   * Only accessible to logged in user with rights to delete post.
   */
  def processPostDeleteAttempt(postId: Int): Action[AnyContent] = authenticatedUserAction { implicit request: Request[AnyContent] =>
    // Get username from cookie
    val deleter = request.session.get(models.Global.SESSION_USERNAME_KEY).get

    // Check if valid deleter is author of post
    val isAuthorOfPostToDelete = postDao.isAuthor(postId, deleter)

    if (isAuthorOfPostToDelete) {
      // Get post and delete it
      val post = postDao.findWithId(postId)
      postWithInfoDao.deletePostAndInfo(post)

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
   * Create an Action to render the edit post visibility page.
   * Only accessible to logged in users with right to edit post visibility.
   */
  def showEditPostVisibility(postId: Int): Action[AnyContent] = authenticatedUserActionWithMessageRequest { implicit request: MessagesRequest[AnyContent] =>
    val username = request.session.get(models.Global.SESSION_USERNAME_KEY).get

    if (postDao.isAuthor(postId, username)) {
      val allUsernames = userDao.findAllOtherUsers(username)
      val postWithInfo = postWithInfoDao.findWithId(postId)
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
  private val editPostVisibilityUrl = routes.PostController.processEditVisibilityAttempt()

  /**
   * Processes a form to edit visibility.
   * Only accessible to logged in users with right to edit post visibility.
   */
  def processEditVisibilityAttempt(): Action[MultipartFormData[play.api.libs.Files.TemporaryFile]] = authenticatedUserActionWithMessageRequest(parse.multipartFormData) { implicit request: MessagesRequest[MultipartFormData[Files.TemporaryFile]] =>
    // Check if all required form fields supplied.
    val visibilitySettingsFromFormCorrect = request.body.dataParts.contains("visibility") && request.body.dataParts.contains("post_id")
    if (visibilitySettingsFromFormCorrect) {
      // Check if valid editor: author of post
      val editor = request.session.get(models.Global.SESSION_USERNAME_KEY).get
      val postId = request.body.dataParts("post_id").toList.head.toInt
      val isValidAuthor = postDao.isAuthor(postId, editor)

      if (isValidAuthor) {
        // Make new visibility object
        val isVisibleToEveryone = request.body.dataParts("visibility").contains("all")
        val listOfVisibleUsernames = request.body.dataParts.getOrElse("shareWithUsernames", List()).toList
        val newVisibilities = Visibility(postId, isVisibleToEveryone, listOfVisibleUsernames)
        val post = postDao.findWithId(postId)

        // Edit visibility
        visibilityDao.editVisibility(post, newVisibilities)

        // Show post
        Redirect(routes.PostController.showPost(postId))
          .flashing("info" -> "Visibility has been changed!")
      } else {
        // Non valid author, which is likely tempered form, perform default action of logging out and showing error.
        Redirect(routes.AuthenticatedUserController
          .logoutWithError("There was something wrong with editing your post. Please try again after logging in."))
      }
    } else {
      // Not all required form fields supplied, form likely been tempered with.
      // Perform default action of logging out and showing error.
      Redirect(routes.AuthenticatedUserController
        .logoutWithError("There was something wrong with editing your post. Please try again after logging in."))
    }
  }

  //---------------------------------------------------------------------------
  //| END EDIT VISIBILITY RELATED FUNCTIONS
  //---------------------------------------------------------------------------
}
