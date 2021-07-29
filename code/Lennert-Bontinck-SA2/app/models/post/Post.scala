package models.post

import java.util.Date

/**
 * Simple case class to represent a user post that has a ID, author username, description and image filename.
 */
case class Post(id: Int,
                author: String,
                date_added: Date,
                description: String,
                image_filename: String)