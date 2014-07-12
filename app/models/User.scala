package models

import org.squeryl.KeyedEntity
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.Table
import org.squeryl.Query
import org.squeryl.dsl.{ OneToMany, ManyToOne }
import collection.Iterable
import play.api.libs.Crypto
import Database._

object Permission {

  def editEvent = (user: User) => user.canEditEvents && default(user)

  def editSales = (user: User) => user.canEditSales && default(user)

  def editUsers = (user: User) => user.canEditUsers && default(user)

  def default = (user: User) => user.isActive && !user.isNew

  def sameUser(userID: Long) = (user: User) => user.id == userID && default(user)

  def anyUser = (user: User) => true

}

case class User(
  id: Long,
  name: String,
  password: String,
  canEditEvents: Boolean,
  canEditSales: Boolean,
  canEditUsers: Boolean,
  isNew: Boolean,
  isActive: Boolean) extends KeyedEntity[Long] {

}

object User {

  def allQ = from(usersTable)(user => select(user) orderBy (user.name asc))

  def getAll = inTransaction(allQ.toList)

  def addNew(user: User) = inTransaction {
    usersTable.insert(
      user copy (password = Crypto.encryptAES(user.password)))
  }

  def findByID(id: Long) = inTransaction {
    usersTable.lookup(id)
  }

  def usersByNameQ(name: String) = usersTable.where(_.name like name)

  def findByName(name: String) = inTransaction {
    usersByNameQ(name).headOption
  }

  def auth(name: String, pass: String): Option[User] = inTransaction {
    findByName(name) match {
      case None => None
      case Some(user) =>
        if (Crypto.decryptAES(user.password) equals pass)
          Some(user)
        else
          None
    }
  }
  def checkPass(pass: String, userID: Long): Boolean = inTransaction {
    val user = findByID(userID).get
    Crypto.encryptAES(pass) == user.password
  }
  def changePassword(newPass: String, userID: Long) = inTransaction {
    update(usersTable)(user =>
      where(user.id === userID)
        set (user.password := Crypto.encryptAES(newPass),
          user.isNew := false))
  }
  def updateUser(user: User) = inTransaction {
    usersTable.update(user copy (password = findByID(user.id).get.password, isNew = false))
  }
  def resetPassword(userID: Long) = inTransaction {
    update(usersTable)(user =>
      where(user.id === userID)
        set (user.password := Crypto.encryptAES("123456"),
          user.isNew := true))
  }
  def disableUser(userID: Long) = inTransaction {
    update(usersTable)(user =>
      where(user.id === userID)
        set (user.isActive := false))
  }
  def enableUser(userID: Long) = inTransaction {
    update(usersTable)(user =>
      where(user.id === userID)
        set (user.isActive := true))
  }
}