package models.comment

import java.util.Date

/**
 * Simple case class to represent a user post that has a ID, author username, description and image filename.
 */
case class Comment(post_id: Int,
                   author: String,
                   date_added: Date,
                   text: String)
