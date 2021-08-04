package models.post

import models.comment.Comment
import models.like.Like
import models.visibility.Visibility

/**
 * Simple case class to represent a post with extra info: comments and likes.
 */
case class PostWithInfo(post: Post,
                        comments: List[Comment],
                        likes: List[Like],
                        visibility: Visibility)

