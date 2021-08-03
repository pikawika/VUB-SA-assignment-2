package models.comment

import models.post.Post

import java.time.LocalDateTime
import javax.inject.Inject

/**
 * Simple Data Access Object (DAO) implementing a naive comment repository.
 * NOTE: no DB is used, per required of the assignment, this is very naive IRL (no hashing, GDPR restrictions,...).
 */
@javax.inject.Singleton
class CommentDao @Inject()() {

  /**
   * Comment variable: the set of comments is kept in memory and has some initial data.
   */
  private var comments = Set(
    Comment(1, "SnellenEddy", LocalDateTime.of(2021, 7, 21, 20, 30), "Ela Ola, why is nobody commenting on my post é! >:("),
    Comment(2, "SnellenEddy", LocalDateTime.of(2021, 7, 21, 20, 30), "Looks good man!"),
    Comment(2, "Lennert", LocalDateTime.of(2021, 7, 21, 20, 31), "Thanks SnellenEddy!"),
    Comment(2, "SnellenEddy", LocalDateTime.of(2021, 7, 21, 20, 32), "No worries man, I really wish I had one like it. Actually, I had one in the past but I crashed it. It was too fast, even for me, SnellenEddy!"),
    Comment(2, "Lennert", LocalDateTime.of(2021, 7, 21, 20, 33), "Haha, well, it's not for sale, at least not for the moment!")
  )

  /**
   * Returns all comments of all posts sorted by the date they were added (oldest first).
   */
  def findAll: List[Comment] = comments.toList.sortBy(_.date_added)

  /**
   * Returns all comments for a single post sorted by the date they were added (oldest first).
   */
  def findForPost(post: Post): List[Comment] = comments.filter(_.post_id == post.id).toList.sortBy(_.date_added)

  /**
   * Adds comment to repository.
   * Creates new object so that exact creation time is used.
   */
  def addComment(comment: Comment): Unit = {
    // Make new comment to have exact time of comment placed
    val comment_with_time = Comment(comment.post_id, comment.author, LocalDateTime.now(), comment.text)
    comments = comments + comment_with_time
  }

}


