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
    if(!postDao.isValidId(id)) {
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
    val like = Like(post_id, liker)

    // Toggle like
    likeDao.toggleLike(like)

    // Goto post page
    Redirect(routes.PostController.showPost(post_id))

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
      // Issues with form itself (validation and/or binding issues)
      // TODO: better error handling
      Redirect(routes.HomeController.showIndex())
    }
    val successFunction = { comment: Comment =>
      // Form validation and binding is correct, check correct user and existing post
      val correct_user = request.session.get(models.Global.SESSION_USERNAME_KEY).get == comment.author
      val correct_post = postDao.isValidId(comment.post_id)

      if(correct_user && correct_post) {
        commentDao.addUser(comment)
        Redirect(routes.PostController.showPost(comment.post_id))
      } else {
        // Form has been most likely tempered with, reroute user to home
        Redirect(routes.HomeController.showIndex())
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
