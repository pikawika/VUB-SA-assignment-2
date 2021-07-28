package models

import javax.inject.Inject

case class Contact (
                  name: String,
                  phone: String
                )

object ContactDao {

    var contacts = Seq(
      Contact("Bob", "0123456789"),
      Contact("Alice", "0156789123"),
      Contact("Tom", "0189123456"))

  def findAll = contacts.toList

  def lookupContact(name: String): Seq[Contact] = {
      //TODO query your database here
      contacts.filter(_.name== name)
    }
}
