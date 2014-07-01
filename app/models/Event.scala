package models
import org.squeryl.KeyedEntity
import java.sql.Timestamp
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.Table
import org.squeryl.Query
import org.squeryl.dsl.{ OneToMany, ManyToOne }
import collection.Iterable

case class Event(
  id: Long,
  tp: String,
  name: String,
  date: Timestamp,
  dscr: String) extends KeyedEntity[Long] {
  lazy val sectors: OneToMany[Sector] =
    Database.eventsToSectors.left(this)
  lazy val sells: OneToMany[Sale] =
    Database.eventsToSells.left(this)
}

object Event {
  import Database.{ eventsTable, sectorsTable, sitsTable }

  def allQ: Query[Event] = from(eventsTable)(event => select(event) orderBy (event.date asc))
  def getAll = inTransaction(allQ.toList)
  def insert(event: Event) = {
    val sectors = (for {
      n <- 1 to 25
    } yield Sector(0, "" + n, 20, 50, 0, 0)).toList
    val vipSectors = for {
      n <- List("VIP A", "VIP D")
    } yield Sector(0, n, 10, 20, 0, 0)
    val allSectors = sectors ++ vipSectors
    inTransaction {
      eventsTable.insert(event)
      Sector.insert(allSectors map (s => event.sectors.assign(s)))
    }
  }
  def update(event: Event) = inTransaction(eventsTable.update(event))
  def save(event: Event) = inTransaction(eventsTable.insertOrUpdate(event))
  def removeById(id: Long) = inTransaction {
    Sector.deleteByEventID(id)
    eventsTable.deleteWhere(_.id === id)
  }
  def getById(id: Long) = getAll.find(_.id == id)
  def getSectors(id: Long): Option[List[Sector]] = getById(id) match {
    case None => None
    case Some(event) => inTransaction(Some(event.sectors.toList))
  }
}