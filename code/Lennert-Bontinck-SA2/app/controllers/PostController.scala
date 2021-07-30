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

  /**
   * Function to process a like attempt, if credentials are correct the like is performed and user if forwarded to post.
   * Otherwise the user is forwarded to index since the form is likely tempered with.
   */
  def processLikeAttempt(post_id: Int): Action[AnyContent] = authenticatedUserAction { implicit request: Request[AnyContent] =>
    val logged_in = request.session.get(models.Global.SESSION_USERNAME_KEY).isDefined

    if (logged_in) {
      val liker = request.session.get(models.Global.SESSION_USERNAME_KEY).get
      val like = Like(post_id, liker)

      val user_has_liked = likeDao.toggleLike(like)

      Redirect(routes.PostController.showPost(post_id))
    } else {
      Redirect(routes.HomeController.showIndex())
    }

  }


  /**
   * Create an Action to render the index HTML page.
   * Only accessible to logged in users.
   */
  def showPost(id: Int): Action[AnyContent] = authenticatedUserAction { implicit request: Request[AnyContent] =>
    val postWithInfo = postWithInfoDao.findWithId(id)
    Ok(views.html.posts.post("Post by " + postWithInfo.post.author, postWithInfo))
  }


}
