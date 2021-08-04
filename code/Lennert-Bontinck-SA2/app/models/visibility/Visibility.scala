package models.visibility

/**
 * Simple case class to represent the visibility of a post.
 */
case class Visibility(post_id: Int,
                      visible_to_all: Boolean,
                      visible_to_usernames: List[String])
