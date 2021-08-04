package models.visibility

import models.post.Post

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
    Visibility(1, visible_to_all = true, List()),
    Visibility(2, visible_to_all = true, List()),
    Visibility(3, visible_to_all = true, List())
  )

  /**
   * Returns the visibility of a post.
   * NOTE: this assumes the ID is valid, perform check first with isValidId method of PostDao!
   */
  def findForPost(post: Post): Visibility = visibilities.find(_.post_id == post.id).get

  /**
   * Adds visibility to repository.
   */
  def addVisibility(visibility: Visibility): Unit = {
    visibilities = visibilities + visibility
  }

  /**
   * Check if post is visible to specified user.
   */
  def userCanViewPost(post: Post, viewing_username: String): Boolean = {
    val visibility = findForPost(post)

    post.author == viewing_username || visibility.visible_to_all || visibility.visible_to_usernames.contains(viewing_username)
  }
}


