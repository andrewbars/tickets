package models
import org.squeryl.KeyedEntity
import java.util.Date
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.Table
import org.squeryl.Query
import collection.Iterable

case class Event(
  id: Long,
  tp: String,
  name: String,
  date: Date,
  dscr: String
  ) extends KeyedEntity[Long]

object Event{
  import Database.eventsTable
  
  def allQ:Query[Event]=from(eventsTable)(event=>select(event) orderBy(event.date asc))
  def getAll = inTransaction(allQ.toList)
  def insert(event:Event)=inTransaction(eventsTable.insert(event))
  def update(event:Event)=inTransaction(eventsTable.update(event))
  def removeById(id:Long)=inTransaction(eventsTable.deleteWhere(_.id===id))
  def getById(id:Long) = getAll.find(_.id==id)
}