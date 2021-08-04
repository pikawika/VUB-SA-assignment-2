package models.comment

import java.time.LocalDateTime

/**
 * Simple case class to represent a comment.
 */
case class Comment(postId: Int,
                   author: String,
                   dateAdded: LocalDateTime,
                   text: String)
