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