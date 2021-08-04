package models.post

import models.comment.CommentDao
import models.like.LikeDao
import models.visibility.VisibilityDao

import javax.inject.Inject

/**
 * Simple Data Access Object (DAO) implementing a naive post with info repository.
 * Since post with info objects are a combination of post, comment and like objects, this DAO uses those repositories.
 */
@javax.inject.Singleton
class PostWithInfoDao @Inject()(postDao: PostDao,
                                commentDao: CommentDao,
                                likeDao: LikeDao,
                                visibilityDao: VisibilityDao) {

  /**
   * Returns all posts with info visible to the supplied username.
   * Sorted by the date they were added in reverse order (newest first) per default.
   * Comments are sorted on date (oldest first).
   * Optional parameter limit_comments: Boolean specifying if all comments should be gathered or only first 3.
   * Optional parameter sort_on_likes: Boolean specifying if post should ne sorted on amount of likes instead.
   */
  def findAll(viewing_username: String, limitComments: Boolean = false, sortOnLikes: Boolean = false): List[PostWithInfo] = {
    // Get all posts (should already be ordered on newest first)
    val posts = postDao.findAll

    // Create empty set to be filled in loop
    var postsWithInfo: Set[PostWithInfo] = Set()

    // Loop over all posts and collect comments and likes, merge them into PostWithInfo objects and add them to set
    for (post <- posts) {
      val visibility = visibilityDao.findForPost(post)

      // Only include posts visible to viewing user
      if (visibilityDao.userCanViewPost(post, viewing_username)) {
        var comments = commentDao.findForPost(post).sortBy(_.dateAdded)

        // If comments is limited, take first 3
        if (limitComments) {
          comments = comments.take(3)
        }

        val likes = likeDao.findForPost(post)

        postsWithInfo = postsWithInfo + PostWithInfo(post, comments, likes, visibility)
      }
    }

    // Sort list and return it
    if (sortOnLikes) {
      // Show most liked first
      postsWithInfo.toList.sortBy(_.likes.length).reverse
    } else {
      // Show newest first
      postsWithInfo.toList.sortBy(_.post.date_added).reverse
    }
  }

  /**
   * Returns the post with info of a specific post ID.
   * NOTE: this assumes the ID is valid and does not check visibility, perform those checks first!
   */
  def findWithId(post_id: Int): PostWithInfo = {
    val post = postDao.findWithId(post_id)
    val comments = commentDao.findForPost(post)
    val likes = likeDao.findForPost(post)
    val visibility = visibilityDao.findForPost(post)

    val postsWithInfo = PostWithInfo(post, comments, likes, visibility)

    postsWithInfo
  }


  /**
   * Returns the posts with info of the specified user.
   * NOTE: this assumes the username is valid, perform check first with uniqueUsername method of UserDao!
   */
  def findFromUser(username: String, viewing_username: String, limit_comments: Boolean = true): List[PostWithInfo] = {
    // Get all posts (should already be ordered on newest first)
    val posts = postDao.findFromUser(username)

    // Create empty set to be filled in loop
    var postsWithInfo: Set[PostWithInfo] = Set()

    // Loop over all posts and collect comments and likes, merge them into PostWithInfo objects and add them to set
    for (post <- posts) {
      // Only include posts visible to viewing user
      if (visibilityDao.userCanViewPost(post, viewing_username)) {
        var comments = commentDao.findForPost(post).sortBy(_.dateAdded)

        // If comments is limited, take first 3
        if (limit_comments) {
          comments = comments.take(3)
        }

        val likes = likeDao.findForPost(post)
        val visibility = visibilityDao.findForPost(post)

        postsWithInfo = postsWithInfo + PostWithInfo(post, comments, likes, visibility)
      }
    }

    // Return list sorted on newest first
    postsWithInfo.toList.sortBy(_.post.date_added).reverse
  }
}


