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
  val numOfRows: Int,
  val seatsInRow: Int,
  val sitPrice: Int,
  val eventID: Long) extends KeyedEntity[Long] {
  lazy val event: ManyToOne[Event] =
    Database.eventsToSectors.right(this)
  lazy val seats: OneToMany[Seat] =
    Database.sectorsToSeats.left(this)
  def updatePrice(newPrice: Int) =
    this.copy(sitPrice = newPrice)
}

case class Seat(
  val id: Long,
  val sectorID: Long,
  val rowNumber: Int,
  val num: Int,
  val sold: Boolean,
  val booked: Boolean,
  val saleID: Option[Long],
  val bookingID: Option[Long]) extends KeyedEntity[Long] {
  def this() = this(0, 0, 0, 0, false, false, Some(0), Some(0))
  lazy val sector: ManyToOne[Sector] =
    Database.sectorsToSeats.right(Seat.this)
  lazy val sell: ManyToOne[Sale] =
    salesToSeats.right(this)
  lazy val booking: ManyToOne[Booking] =
    bookingsToSeats.right(this)
  def sell(sID: Long) = this.copy(saleID = Some(sID))
}

object Sector {

  val sectorNames = (for (n <- (1 to 25)) yield n.toString).toList ::: List("VIP A", "VIP D")
  def insert(sectors: List[Sector]) = {
    inTransaction {
      sectorsTable.insert(sectors)
    }
  }
  def event(sector: Sector) = inTransaction(sector.event.single)
  def updatePrices(eventID: Long, newPrices: Map[String, Int]) = inTransaction {
    sectorsTable.update {
      val event = Event.getById(eventID).get
      event.sectors map (sector => sector updatePrice (newPrices getOrElse (sector.name, 0)))
    }
  }
  def getByID(id: Long) = inTransaction {
    sectorsTable.lookup(id)
  }
  def sectorsByEventIdQ(id: Long) = inTransaction {
    sectorsTable.where(s => s.eventID === id)
  }
  def deleteByEventID(id: Long) = inTransaction {
    Seat.deleteAllByEventID(id)
    sectorsTable.delete(sectorsByEventIdQ(id))
  }
  def orderedSeatsInSector(sector: Sector) = inTransaction {
    sector.seats.toList
  }
  def seats(sector: Sector) = inTransaction(sector.seats.toList)
}

object Seat {
  def insert(sits: Iterable[Seat]) = inTransaction(Database.sitsTable.insert(sits))
  def update(sits: List[Seat]) = inTransaction(Database.sitsTable.update(sits))
  def deleteOne(seatID:Long) = inTransaction(sitsTable.deleteWhere(_.id===seatID))
  def seatsByEventIdQ(id: Long) = from(sitsTable, sectorsTable) { (seat, sector) =>
    where(sector.eventID === id and seat.sectorID === sector.id)
    select(seat)
  }
  def deleteAllByEventID(id: Long) = inTransaction {
    sitsTable.delete(seatsByEventIdQ(id))
  }
  def sector(seat: Seat) = inTransaction {
    seat.sector.single
  }
  def sale(seat: Seat) = inTransaction(Sector.event(Seat.sector(seat)))
  def booking(seat: Seat) = inTransaction(seat.booking.single)
}