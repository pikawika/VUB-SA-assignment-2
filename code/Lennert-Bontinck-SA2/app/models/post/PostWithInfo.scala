package models.post

import models.comment.Comment


/**
 * Simple case class to represent a post with it's info (comments and likes).
 */
case class PostWithInfo(post: Post,
                        comments: List[Comment])

