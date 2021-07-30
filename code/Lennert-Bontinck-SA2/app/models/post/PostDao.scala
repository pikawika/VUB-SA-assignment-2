package models.post

import models.post

import java.time.LocalDateTime
import javax.inject.Inject

/**
 * Simple Data Access Object (DAO) implementing a naive post repository.
 * NOTE: no DB is used, per required of the assignment, this is very naive IRL (no hashing, GDPR restrictions,...).
 */
@javax.inject.Singleton
class PostDao @Inject()() {

  /**
   * Posts variable: the set of posts is kept in memory and has some initial data.
   */
  private var posts = Set(
    post.Post(1, "SnellenEddy", LocalDateTime.of(2021, 7, 20, 20, 30), "The new electric 208, it's fast! I also add this very long text to show overflow doesn't occur. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nunc mattis ultrices enim sit amet euismod. Ut eget arcu tincidunt, ullamcorper neque eget, elementum mi. Sed non turpis maximus, vulputate dui a, facilisis purus. In finibus ante congue, tempor augue in, malesuada odio. Donec suscipit varius enim, non interdum nisl consectetur quis. Vestibulum venenatis neque at interdum tristique. Morbi nec scelerisque diam.", "electric208.jpeg"),
    post.Post(2, "Lennert", LocalDateTime.of(2021, 7, 21, 19, 30), "My awesome 106 GTi, finally finished after more then 3 years of revision!", "106gti.jpeg"),
  )

  /**
   * Returns all posts sorted by the date they were added in reverse order (newest first).
   */
  def findAll: List[Post] = posts.toList.sortBy(_.date_added).reverse

  /**
   * Returns the post with a specific ID.
   * NOTE: this assumes the ID is valid, perform check first with isValidId method!
   */
  def findWithId(id: Int): Post = {
    posts.find(_.id == id).get
  }

  /**
   * Returns whether or not an ID is a valid post ID.
   */
  def isValidId(id: Int): Boolean = {
    posts.exists(_.id == id)
  }

}


