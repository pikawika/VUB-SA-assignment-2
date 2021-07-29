package models

import javax.inject.Inject
import scala.util.control.Breaks.{break, breakable}

/**
 * Simple Data Access Object (DAO) implementing a naive post repository.
 * No database is used as posts are just saved in memory (per requirement of assignment).
 */
@javax.inject.Singleton
class PostDao @Inject()() {

  /**
   * Keep posts in memory.
   * NOTE: this is very naive but required by assignment.
   */
  private var posts = Set(
    Post(1, "Lennert", "My awesome 106 GTi, finally finished after more then 3 years of revision!", "106gti.jpeg"),
    Post(1, "SnellenEddy", "The new electric 208, it's fast!", "electric208.jpeg"),
  )


  def findAll: List[Post] = posts.toList

}


