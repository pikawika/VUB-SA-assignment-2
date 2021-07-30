package models.post

import models.comment.CommentDao
import models.like.LikeDao

import javax.inject.Inject

/**
 * Simple Data Access Object (DAO) implementing a naive post with info repository.
 * Since post with info objects are a combination of post, comment and like objects, this DAO uses those repositories.
 */
@javax.inject.Singleton
class PostWithInfoDao @Inject()(postDao: PostDao, commentDao: CommentDao, likeDao: LikeDao) {

  /**
   * Returns all posts with info sorted by the date they were added in reverse order (newest first).
   * Comments are sorted on date (oldest first).
   * Optional parameter limit_comments: Boolean specifying if all comments should be gathered or only first 3.
   * Optional parameter sort_on_likes: Boolean specifying if post should ne sorted on amount of likes instead.
   */
  def findAll(limit_comments: Boolean = false, sort_on_likes: Boolean = false): List[PostWithInfo] = {
    // Get all posts (should already be ordered on newest first)
    val posts = postDao.findAll

    // Create empty set to be filled in loop
    var postsWithInfo: Set[PostWithInfo] = Set()

    // Loop over all posts and collect comments and likes, merge them into PostWithInfo objects and add them to set
    for (post <- posts) {
      var comments = commentDao.findForPost(post).sortBy(_.date_added)

      // If comments is limited, take first 3
      if (limit_comments) {
        comments = comments.take(3)
      }

      val likes = likeDao.findForPost(post)

      postsWithInfo = postsWithInfo + PostWithInfo(post, comments, likes)
    }

    // Sort list and return it
    if (sort_on_likes) {
      // Show most liked first
      postsWithInfo.toList.sortBy(_.likes.length).reverse
    } else {
      // Show newest first
      postsWithInfo.toList.sortBy(_.post.date_added).reverse
    }

  }

  /**
   * Returns the post with info of a specific post ID.
   * NOTE: this assumes the ID is valid, perform check first with isValidId method of PostDao!
   */
  def findWithId(post_id: Int): PostWithInfo = {
    val post = postDao.findWithId(post_id)
    val comments = commentDao.findForPost(post)
    val likes = likeDao.findForPost(post)

    val postsWithInfo = PostWithInfo(post, comments, likes)

    postsWithInfo
  }
}


