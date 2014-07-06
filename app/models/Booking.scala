package models

import org.squeryl.KeyedEntity
import java.sql.Timestamp
import java.util.Date
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.Table
import org.squeryl.Query
import org.squeryl.dsl.{ OneToMany, ManyToOne }
import collection.Iterable
import Database._

case class Booking(
  id: Long,
  eventID: Long,
  date: Timestamp,
  clientName: String,
  expDate: Timestamp,
  price: Int,
  confirmed: Boolean) extends KeyedEntity[Long] {
  lazy val seats: OneToMany[Seat] =
    bookingsToSeats.left(this)
  lazy val event: ManyToOne[Event] =
    eventsToBookings.right(this)
}

object Booking {

  def getById(bookingID: Long) =
    inTransaction(bookingsTable.lookup(bookingID))
  def addNew(sitsMap: List[List[Int]], sectorID: Long, booking: Booking) = inTransaction {
    val sector = Sector.getByID(sectorID).get
    val event = sector.event.single
    bookingsTable.insert(booking)
    val seatstoInsert = (for {
      row <- sitsMap.zipWithIndex
      num <- row._1
    } yield Seat(0, sectorID, row._2 + 1, num, false, true, None, Some(booking.id)))
    Seat.insert(seatstoInsert)
  }
  def seats(booking: Booking) = inTransaction(booking.seats.toList)
  def confirmBooking(booking: Booking) = inTransaction(bookingsTable.update(booking.copy(confirmed = true)))
  def revertBooking(booking: Booking) = inTransaction {
    sitsTable.delete(booking.seats)
    bookingsTable.deleteWhere(_.id === booking.id)
  }
  def event(booking:Booking) = inTransaction(booking.event.single)
  def bookingsByNameQ(name:String)=bookingsTable.where(booking=>booking.clientName like name)
  def findByClientName(searchVal:String)=inTransaction(bookingsByNameQ(searchVal).toList)
}