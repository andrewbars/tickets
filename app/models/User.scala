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

  def editEvent = (user: User) => user.canEditEvents

  def editSales = (user: User) => user.canEditSales

  def editUsers = (user: User) => user.canEditUsers
  
  def default = (user:User)=>true

}

case class User(
  id: Long,
  name: String,
  password: String,
  canEditEvents: Boolean,
  canEditSales: Boolean,
  canEditUsers: Boolean) extends KeyedEntity[Long] {

}

object User {
  
  def allQ = from(usersTable)(user=>select(user) orderBy (user.name asc))
  
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

  def auth(name: String, pass: String):Option[User] = inTransaction {
    findByName(name) match {
      case None => None
      case Some(user) =>
        if (Crypto.decryptAES(user.password) equals pass)
          Some(user)
        else
          None
    }
  }
}