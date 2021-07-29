package models.post

import models.comment.CommentDao

import javax.inject.Inject

/**
 * Simple Data Access Object (DAO) implementing a naive post with info repository.
 * No database is used as items are just saved in memory (per requirement of assignment).
 */
@javax.inject.Singleton
class PostWithInfoDao @Inject()(postDao: PostDao, commentDao: CommentDao) {
  def findAll: List[PostWithInfo] = {
    val posts = postDao.findAll

    var postsWithInfo: Set[PostWithInfo] = Set()


    for (post <- posts) {
      val comments = commentDao.findForPost(post)
      postsWithInfo = postsWithInfo + PostWithInfo(post, comments)
    }

    postsWithInfo.toList
  }

  def findAllWithFewComments: List[PostWithInfo] = {
    val posts = postDao.findAll

    var postsWithInfo: Set[PostWithInfo] = Set()


    for (post <- posts) {
      val comments = commentDao.findFewForPost(post)
      postsWithInfo = postsWithInfo + PostWithInfo(post, comments)
    }

    postsWithInfo.toList
  }

}


