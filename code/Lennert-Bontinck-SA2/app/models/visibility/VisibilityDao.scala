package models.visibility

import models.post.{Post, PostDao}

import javax.inject.Inject

/**
 * Simple Data Access Object (DAO) implementing a naive visibility repository.
 * NOTE: no DB is used, per required of the assignment, this is very naive IRL (no hashing, GDPR restrictions,...).
 */
@javax.inject.Singleton
class VisibilityDao @Inject()() {

  /**
   * Visibilities variable: the set of visibility objects is kept in memory and has some initial data.
   */
  private var visibilities = Set(
    Visibility(1, isVisibleToEveryone = true, List()),
    Visibility(2, isVisibleToEveryone = true, List()),
    Visibility(3, isVisibleToEveryone = true, List()),
    Visibility(4, isVisibleToEveryone = false, List("SnellenEddy"))
  )

  /**
   * Returns the visibility of a post.
   * NOTE: this assumes the ID is valid, perform check first with isValidId method of PostDao!
   */
  def findForPost(post: Post): Visibility = visibilities.find(_.postId == post.id).get

  /**
   * Adds visibility object to repository.
   */
  def addVisibility(visibility: Visibility): Unit = {
    visibilities = visibilities + visibility
  }

  /**
   * Edits visibility of item in repository.
   * NOTE: this assumes the ID is valid, perform check first with isValidId method of PostDao!
   */
  def editVisibility(post: Post, newVisibility: Visibility): Unit = {
    val oldVisibility = findForPost(post)
    visibilities = visibilities - oldVisibility
    visibilities = visibilities + newVisibility
  }

  /**
   * Check if post is visible to a specified user.
   */
  def userCanViewPost(post: Post, viewingUsername: String): Boolean = {
    val visibility = findForPost(post)

    post.author == viewingUsername || visibility.isVisibleToEveryone || visibility.listOfVisibleUsernames.contains(viewingUsername)
  }

  /**
   * Deletes all visibility objects for given post.
   */
  def deleteVisibilityForPost(post: Post): Unit = {
    visibilities = visibilities.filter(_.postId != post.id)
  }
}


