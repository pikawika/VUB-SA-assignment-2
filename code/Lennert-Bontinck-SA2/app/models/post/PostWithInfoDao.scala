package models.post

import models.comment.CommentDao
import models.like.LikeDao

import javax.inject.Inject

/**
 * Simple Data Access Object (DAO) implementing a naive post with info repository.
 * No database is used as items are just saved in memory (per requirement of assignment).
 */
@javax.inject.Singleton
class PostWithInfoDao @Inject()(postDao: PostDao, commentDao: CommentDao, likeDao: LikeDao) {
  def findAll: List[PostWithInfo] = {
    val posts = postDao.findAll

    var postsWithInfo: Set[PostWithInfo] = Set()


    for (post <- posts) {
      val comments = commentDao.findForPost(post)
      val likes = likeDao.findForPost(post)
      postsWithInfo = postsWithInfo + PostWithInfo(post, comments, likes)
    }

    postsWithInfo.toList
  }

  def findAllWithFewComments: List[PostWithInfo] = {
    val posts = postDao.findAll

    var postsWithInfo: Set[PostWithInfo] = Set()


    for (post <- posts) {
      val comments = commentDao.findFewForPost(post)
      val likes = likeDao.findForPost(post)
      postsWithInfo = postsWithInfo + PostWithInfo(post, comments, likes)
    }

    postsWithInfo.toList
  }

  def findWithId(id: Int): PostWithInfo = {
    val post = postDao.findWithId(id)
    val comments = commentDao.findFewForPost(post)
    val likes = likeDao.findForPost(post)

    val postsWithInfo = PostWithInfo(post, comments, likes)

    postsWithInfo
  }
}


