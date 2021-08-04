package models.post

import java.time.LocalDateTime

/**
 * Simple case class to represent a user post.
 */
case class Post(id: Int,
                author: String,
                dateAdded: LocalDateTime,
                description: String,
                imageFilename: String)