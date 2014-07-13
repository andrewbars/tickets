package models

import org.squeryl.KeyedEntity
import java.sql.Timestamp
import java.util.{ Date, Calendar }
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
  confirmed: Boolean,
  userID: Long) extends KeyedEntity[Long] {
  lazy val seats: OneToMany[Seat] =
    bookingsToSeats.left(this)
  lazy val event: ManyToOne[Event] =
    eventsToBookings.right(this)
  lazy val user: ManyToOne[User] =
    usersToBookings.right(this)
}

object Booking {

  def getAllQ = from(bookingsTable)(booking=>select(booking) orderBy(booking.eventID asc))
  
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
  def event(booking: Booking) = inTransaction(booking.event.single)
  def bookingsByNameQ(name: String) = bookingsTable.where(booking => booking.clientName like name)
 
  
  def findByClientName(searchVal: String) = inTransaction{
    val searchWords = searchVal.split("\\s+")
    val bookings = getAllQ.toList
    bookings filter {booking=>
      val clientNameSeparated = booking.clientName.split("\\s+")
      searchWords forall (word=>clientNameSeparated exists(_ startsWith word))
    }
  }
  
  
  def redeemBooking(booking: Booking, sale: Sale) = inTransaction {
    salesTable.insert(sale)
    val seatsToUpdate = seats(booking) map (seat => seat.copy(sold = true, booked = false, bookingID = None, saleID = Some(sale.id)))
    sitsTable.update(seatsToUpdate)
    bookingsTable.deleteWhere(_.id === booking.id)
  }

  def expire = inTransaction {
    val bookings = bookingsTable.where(booking =>
      booking.expDate < new Timestamp(Calendar.getInstance().getTimeInMillis()))
    /*  TODO Why this thing doesn`t wokk?!!!!
    val seats =from(bookingsTable,sitsTable)((booking, seat)=>
    	where(booking.expDate < new Timestamp(Calendar.getInstance().getTimeInMillis()) and seat.bookingID===booking.id)
    	select(seat)
    )
    
    sitsTable.delete(seats)
    * */
    for (booking <- bookings) (booking.seats.deleteAll)
    bookingsTable.delete(bookings)
  }
  
  def user(booking:Booking) = inTransaction{
    booking.user.single
  }
}