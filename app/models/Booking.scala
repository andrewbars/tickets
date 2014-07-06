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
  expDate: Timestamp,
  price: Int,
  confirmed: Boolean) extends KeyedEntity[Long] {
  lazy val seats: OneToMany[Seat] =
    bookingsToSeats.left(this)
  lazy val event: ManyToOne[Event] =
    eventsToBookings.right(this)
}

object Booking {
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
}