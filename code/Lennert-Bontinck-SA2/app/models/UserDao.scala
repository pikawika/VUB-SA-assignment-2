package models

import javax.inject.Inject
import scala.util.control.Breaks.{break, breakable}

/**
 * Simple Data Access Object (DAO) implementing a naive user repository.
 * No database is used as users are just saved in memory (per requirement of assignment).
 */
@javax.inject.Singleton
class UserDao @Inject()() {

    /**
     * Keep users in memory.
     * NOTE: this is very naive and passwords are not encrypted but as required of assignment.
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

}


