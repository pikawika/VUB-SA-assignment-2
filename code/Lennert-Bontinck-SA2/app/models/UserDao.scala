package models

import javax.inject.Inject

@javax.inject.Singleton
class UserDao @Inject()() {

    var users = Seq(
        User("user", "user"),
        User("Snellen Eddy", "AmazingPassword"),
        User("Johny Bravo", "AmazingPassword")
    )
    def lookupUser(u: User): Boolean = {
        //TODO query your database here
        if (users.contains(u)) true else false
    }

}


