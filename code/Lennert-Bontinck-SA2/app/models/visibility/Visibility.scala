package models.visibility

/**
 * Simple case class to represent the visibility of a post.
 */
case class Visibility(postId: Int,
                      isVisibleToEveryone: Boolean,
                      listOfVisibleUsernames: List[String])
