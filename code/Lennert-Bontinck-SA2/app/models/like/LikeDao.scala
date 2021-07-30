package models.like

import models.post.Post

import java.util.Calendar
import javax.inject.Inject

/**
 * Simple Data Access Object (DAO) implementing a naive like repository.
 * No database is used as likes are just saved in memory (per requirement of assignment).
 */
@javax.inject.Singleton
class LikeDao @Inject()() {

  /**
   * Keep likes in memory.
   * NOTE: this is very naive but required by assignment.
   */
  private var likes = Set(
    Like(1, "Lennert"),
    Like(1, "SnellenEddy"),
    Like(2, "SnellenEddy")
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
   * Returns true if user has liked post after performing toggle.
   */
  def toggleLike(like: Like) : Boolean = {
    if(likes.contains(like)) {
      likes = likes - like
      false
    } else {
      likes = likes + like
      true
    }
  }

}


