package models

/**
 * Simple case class to represent a user post that has a ID, author username, description and image filename.
 */
case class Post(
    id: Int,
    author: String,
    description: String,
    image_filename: String
)

