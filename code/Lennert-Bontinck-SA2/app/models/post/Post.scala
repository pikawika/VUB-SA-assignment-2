package models.post

import java.time.LocalDateTime

/**
 * Simple case class to represent a user post.
 */
case class Post(id: Int,
                author: String,
                date_added: LocalDateTime,
                description: String,
                image_filename: String)
