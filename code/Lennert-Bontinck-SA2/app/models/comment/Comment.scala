package models.comment

import java.util.Date

/**
 * Simple case class to represent a comment.
 */
case class Comment(post_id: Int,
                   author: String,
                   date_added: Date,
                   text: String)
