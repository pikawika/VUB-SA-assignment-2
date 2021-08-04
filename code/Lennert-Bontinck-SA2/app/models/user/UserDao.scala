package models.user

import javax.inject.Inject

/**
 * Simple Data Access Object (DAO) implementing a naive user repository.
 * NOTE: no DB is used, per required of the assignment, this is very naive IRL (no hashing, GDPR restrictions,...).
 */
@javax.inject.Singleton
class UserDao @Inject()() {

    /**
     * Users variable: the set of users is kept in memory and has some initial data.
     * Username can be used as unique identifier.
     */
    private var users = Set(
        User("Lennert", "AmazingPassword"),
        User("SnellenEddy", "AmazingPassword"),
        User("JohnyBravo", "AmazingPassword")
    )

    /**
     * Returns true if passed user object corresponds with a registered user, false otherwise.
     */
    def authenticateUser(user: User): Boolean = {
        users.contains(user)
    }

    /**
     * Returns true if specified username is an unique username for a new user.
     */
    def uniqueUsername(username: String): Boolean = {
        !users.exists(_.username.toLowerCase() == username.toLowerCase())
    }

    /**
     * Returns true if passed user object is added to list of registered users, false otherwise.
     * Fail will occur if username already exists.
     */
    def addUser(user: User): Boolean = {
        if(uniqueUsername(user.username)) {
            users = users + user
            true
        } else {
            false
        }
    }

    /**
     * Returns all usernames excepts from supplied current user sorted alphabetically.
     */
    def findAllOtherUsers(currentUser: String): List[String] = users.toList.sortBy(_.username).map(_.username).filter(_ != currentUser)

}


