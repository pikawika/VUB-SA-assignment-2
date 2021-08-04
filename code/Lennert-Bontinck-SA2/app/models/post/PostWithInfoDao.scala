package models.post

import models.comment.CommentDao
import models.like.LikeDao
import models.visibility.VisibilityDao

import javax.inject.Inject

/**
 * Simple Data Access Object (DAO) implementing a naive post with info repository.
 * Since post with info objects are a combination of post, comment, like and visibility objects, this DAO uses those repositories.
 */
@javax.inject.Singleton
class PostWithInfoDao @Inject()(postDao: PostDao,
                                commentDao: CommentDao,
                                likeDao: LikeDao,
                                visibilityDao: VisibilityDao) {

  /**
   * Creates post with info objects from a list of individual objects.
   */
  private def createPostWithInfoObjectsForUser(usernameOfViewer: String, limitComments: Boolean, posts: List[Post]): Set[PostWithInfo] = {
    // Create empty set to be filled in loop
    var postsWithInfo: Set[PostWithInfo] = Set()

    // Loop over all posts and collect comments and likes, merge them into PostWithInfo objects and add them to set
    for (post <- posts) {
      val visibility = visibilityDao.findForPost(post)

      // Only include posts visible to viewing user
      if (visibilityDao.userCanViewPost(post, usernameOfViewer)) {
        var comments = commentDao.findForPost(post).sortBy(_.dateAdded)

        // If comments is limited, take first 3
        if (limitComments) {
          comments = comments.take(3)
        }

        val likes = likeDao.findForPost(post)

        postsWithInfo = postsWithInfo + PostWithInfo(post, comments, likes, visibility)
      }
    }

    postsWithInfo
  }

  /**
   * Returns all posts with info visible to the supplied username.
   * Sorted by the date they were added in reverse order (newest first) per default.
   * Comments are sorted on date (oldest first).
   * Optional parameter limitComments: Boolean specifying if all comments should be gathered or only first 3.
   * Optional parameter sortOnLikes: Boolean specifying if post should be sorted on amount of likes.
   */
  def findAll(usernameOfViewer: String, limitComments: Boolean = false, sortOnLikes: Boolean = false): List[PostWithInfo] = {
    // Get all posts (should already be ordered on newest first)
    val posts = postDao.findAll

    val postsWithInfo = createPostWithInfoObjectsForUser(usernameOfViewer, limitComments, posts)

    // Sort list and return it
    if (sortOnLikes) {
      // Show most liked first
      postsWithInfo.toList.sortBy(_.likes.length).reverse
    } else {
      // Show newest first
      postsWithInfo.toList.sortBy(_.post.dateAdded).reverse
    }
  }

  /**
   * Returns the post with info of a specific post ID.
   * NOTE: this assumes the ID is valid and does not check visibility, perform those checks first using the right DAO!
   */
  def findWithId(postId: Int): PostWithInfo = {
    val post = postDao.findWithId(postId)
    val comments = commentDao.findForPost(post)
    val likes = likeDao.findForPost(post)
    val visibility = visibilityDao.findForPost(post)

    val postsWithInfo = PostWithInfo(post, comments, likes, visibility)

    postsWithInfo
  }


  /**
   * Returns the posts with info of a specified user.
   */
  def findFromUser(username: String, usernameOfViewer: String, limitComments: Boolean = true): List[PostWithInfo] = {
    // Get all posts (should already be ordered on newest first)
    val posts = postDao.findFromUser(username)

    val postsWithInfo = createPostWithInfoObjectsForUser(usernameOfViewer, limitComments, posts)

    // Return list sorted on newest first
    postsWithInfo.toList.sortBy(_.post.dateAdded).reverse
  }

  /**
   * Deletes the passed post object and all of it's related objects.
   */
  def deletePostAndInfo(post: Post): Unit = {
    postDao.deletePost(post)
    commentDao.deleteCommentsForPost(post)
    likeDao.deleteLikesForPost(post)
    visibilityDao.deleteVisibilityForPost(post)
  }
}


