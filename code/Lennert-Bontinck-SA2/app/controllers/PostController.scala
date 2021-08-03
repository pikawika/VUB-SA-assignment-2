package controllers

import models.comment.{Comment, CommentDao}
import models.like.{Like, LikeDao}
import models.post.{PostDao, PostWithInfoDao}
import play.api.data.Form
import play.api.data.Forms.{localDateTime, mapping, nonEmptyText, number}
import play.api.mvc._

import java.time.LocalDateTime
import javax.inject.Inject

/**
 * This controller is responsible for handling HTTP requests
 * to the application's post related pages through its actions.
 */
class PostController @Inject()(cc: MessagesControllerComponents,
                               authenticatedUserAction: AuthenticatedUserAction,
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
   * Only accessible to logged in users.
   */
  def showPost(id: Int): Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    if (!postDao.isValidId(id)) {
      // If ID is invalid go to home
      Redirect(routes.HomeController.showIndex())
    } else {
      // Get post object
      val postWithInfo = postWithInfoDao.findWithId(id)

      // Display post object
      val username = request.session.get(models.Global.SESSION_USERNAME_KEY).get
      val filledCommentForm = commentForm.fill(Comment(postWithInfo.post.id, username, LocalDateTime.now(), ""))
      Ok(views.html.posts.post("Post by " + postWithInfo.post.author, postWithInfo, filledCommentForm, commentPostUrl))
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
    // Get username from cookie and create like object
    val liker = request.session.get(models.Global.SESSION_USERNAME_KEY).get

    // Check if a valid post id is given
    val valid_id = postDao.isValidId(post_id)

    if (valid_id) {
      // Toggle like
      val like = Like(post_id, liker)
      val now_liked = likeDao.toggleLike(like)

      // Goto post page and show right flash
      if(now_liked) {
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
   * The user register form and it's verification.
   * Checks if username and password are of correct length and format.
   * Checks if username is not already taken.
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
   * The submit URL of the user register form.
   */
  private val commentPostUrl = routes.PostController.processCommentAttempt()

  /**
   * Function to process a comment attempt, forwards user to post on which (s)he commented.
   */
  def processCommentAttempt(): Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    val errorFunction = { formWithErrors: Form[Comment] =>
      // Issues with form itself (validation and/or binding issues).
      // If a valid post ID can be found display error on post page.
      // If no valid POST id is found; unexpected form data, perform default action of logging out and showing error.
      val post_id = formWithErrors.data.getOrElse("post_id", -1).toString.toIntOption.getOrElse(-1)
      if (postDao.isValidId(post_id)) {
        val postWithInfo = postWithInfoDao.findWithId(post_id)
        BadRequest(views.html.posts.post("Commenting failed", postWithInfo, formWithErrors, commentPostUrl))
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


}
