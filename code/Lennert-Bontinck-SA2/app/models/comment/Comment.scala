package models.comment

import java.time.LocalDateTime

/**
 * Simple case class to represent a comment.
 */
case class Comment(post_id: Int,
                   author: String,
                   date_added: LocalDateTime,
                   text: String)
