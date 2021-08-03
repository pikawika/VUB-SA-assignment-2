package models.like

import models.post.Post

import javax.inject.Inject

/**
 * Simple Data Access Object (DAO) implementing a naive like repository.
 * NOTE: no DB is used, per required of the assignment, this is very naive IRL (no hashing, GDPR restrictions,...).
 */
@javax.inject.Singleton
class LikeDao @Inject()() {

  /**
   * Likes variable: the set of likes is kept in memory and has some initial data.
   */
  private var likes = Set(
    Like(2, "Lennert"),
    Like(2, "SnellenEddy"),
    Like(1, "SnellenEddy")
  )

  /**
   * Returns all likes of all posts.
   */
  def findAll: List[Like] = likes.toList

  /**
   * Returns all likes for a single post.
   */
  def findForPost(post: Post): List[Like] = likes.filter(_.post_id == post.id).toList

  /**
   * Toggles passed like object: adds or removes it.
   * Returns bool that says if user has liked post after toggle or not.
   */
  def toggleLike(like: Like): Boolean = {
    if (likes.contains(like)) {
      likes = likes - like
    } else {
      likes = likes + like
    }

    // Return if user liked or not
    likes.contains(like)
  }

}


