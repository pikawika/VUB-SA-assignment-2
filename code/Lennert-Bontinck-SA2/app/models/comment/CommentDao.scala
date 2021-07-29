package models.comment

import models.post.Post

import java.util.Calendar
import javax.inject.Inject

/**
 * Simple Data Access Object (DAO) implementing a naive comment repository.
 * No database is used as comments are just saved in memory (per requirement of assignment).
 */
@javax.inject.Singleton
class CommentDao @Inject()() {

  /**
   * Keep comments in memory.
   * NOTE: this is very naive but required by assignment.
   */
  private var comments = Set(
    Comment(1, "SnellenEddy", Calendar.getInstance().getTime, "Looks good man!"),
    Comment(1, "Lennert", Calendar.getInstance().getTime, "Thanks SnellenEddy!"),
    Comment(1, "SnellenEddy", Calendar.getInstance().getTime, "No worries man, I really wish I had one like it."),
    Comment(1, "Lennert", Calendar.getInstance().getTime, "Haha, well, it's not for sale, at least not for the moment!")
  )

  /**
   * Returns all comments of all posts.
   */
  def findAll: List[Comment] = comments.toList

  /**
   * Returns all comments for a single post.
   */
  def findForPost(post: Post): List[Comment] = comments.filter(_.post_id == post.id).toList

  /**
   * Returns first three comments for a single post.
   */
  def findFewForPost(post: Post): List[Comment] = comments.filter(_.post_id == post.id).toList.take(3)

}


