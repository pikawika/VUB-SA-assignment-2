package controllers

import models.like.{Like, LikeDao}
import models.post.PostWithInfoDao
import play.api.mvc._

import javax.inject.Inject

/**
 * This controller is responsible for handling HTTP requests
 * to the application's post related pages through its actions.
 */
class PostController @Inject()(cc: MessagesControllerComponents,
                               authenticatedUserAction: AuthenticatedUserAction,
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
  def showPost(id: Int): Action[AnyContent] = authenticatedUserAction { implicit request: Request[AnyContent] =>
    // Get post object
    val postWithInfo = postWithInfoDao.findWithId(id)

    // Display post object
    Ok(views.html.posts.post("Post by " + postWithInfo.post.author, postWithInfo))
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
   * Function to process a comment attempt, forwards user to post on which (s)he commented.
   */
  def processCommentAttempt(): Action[AnyContent] = authenticatedUserAction { implicit request: Request[AnyContent] =>
    //todo
    Redirect(routes.HomeController.showIndex())
  }


  //---------------------------------------------------------------------------
  //| END COMMENT RELATED FUNCTIONS
  //---------------------------------------------------------------------------





}
