package models.post

import models.post

import java.util.Calendar
import javax.inject.Inject

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
    post.Post(1, "Lennert", Calendar.getInstance().getTime, "My awesome 106 GTi, finally finished after more then 3 years of revision!", "106gti.jpeg"),
    post.Post(2, "SnellenEddy", Calendar.getInstance().getTime, "The new electric 208, it's fast! I also add this very long text to show overflow doesn't occur. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nunc mattis ultrices enim sit amet euismod. Ut eget arcu tincidunt, ullamcorper neque eget, elementum mi. Sed non turpis maximus, vulputate dui a, facilisis purus. In finibus ante congue, tempor augue in, malesuada odio. Donec suscipit varius enim, non interdum nisl consectetur quis. Vestibulum venenatis neque at interdum tristique. Morbi nec scelerisque diam.", "electric208.jpeg"),
  )


  def findAll: List[Post] = posts.toList

}


