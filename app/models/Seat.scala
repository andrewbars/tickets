package models

import org.squeryl.KeyedEntity
import java.sql.Timestamp
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.Table
import org.squeryl.Query
import org.squeryl.dsl.{ OneToMany, ManyToOne }
import collection.Iterable
import Database._

case class Sector(
  val id: Long,
  val name: String,
  val numOfSeats: Int,
  val sitPrice: Int,
  val eventID: Long) extends KeyedEntity[Long] {
  lazy val event: ManyToOne[Event] =
    Database.eventsToSectors.right(this)
  lazy val seats: OneToMany[Seat] =
  Database.sectorsToSeats.left(this)
  def updatePrice(newPrice: Int) =
    Sector(id, name, numOfSeats, newPrice, eventID)
}

case class Seat(
  val id: Long,
  val sectorID: Long,
  val rowNumber: Int,
  val num: Int,
  val sold: Boolean,
  val sellID: Option[Long]) extends KeyedEntity[Long] {
  def this() = this(0, 0, 0, 0, false, Some(0))
  lazy val sector: ManyToOne[Sector] =
    Database.sectorsToSeats.right(Seat.this)
  lazy val sell: ManyToOne[Sale] =
    salesToSeats.right(this)
    def sell(sID:Long)=Seat(id,sectorID,rowNumber,num,true,Some(sID))
}

object Sector {

  val sectorNames = (for (n <- (1 to 25)) yield n.toString).toList ::: List("VIP A", "VIP D")
  def insert(sectors: List[Sector]) = {
    inTransaction {
      sectorsTable.insert(sectors)
      val sits = for {
        s <- sectors
        r <- if (s.numOfSeats == 1000) (1 to 20) else (1 to 10)
        n <- if (s.numOfSeats == 1000) (1 to 50) else (1 to 20)
      } yield Seat(0, s.id, r, n, false, None)
      Seat.insert(sits)
    }
  }
  def updatePrices(eventID: Long, newPrices: Map[String, Int]) = inTransaction {
    sectorsTable.update {
      val event = Event.getById(eventID).get
      event.sectors map (sector => sector updatePrice (newPrices getOrElse (sector.name, 0)))
    }
  }
  def getByID(id: Long) = inTransaction {
    sectorsTable.lookup(id)
  }
}

object Seat {
  def insert(sits: List[Seat]) = inTransaction(Database.sitsTable.insert(sits))
  def update(sits: List[Seat]) = inTransaction(Database.sitsTable.update(sits))
}