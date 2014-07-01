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

case class Sale(
  id: Long,
  eventID: Long,
  date: Timestamp) extends KeyedEntity[Long] {
  lazy val event: ManyToOne[Event] =
    Database.eventsToSells.right(Sale.this)
  lazy val sits: OneToMany[Seat] =
    salesToSeats.left(Sale.this)
}

object Sale {
  def addNew(sitsMap: List[List[Int]], sectorID: Long) = inTransaction {
    val sector = Sector.getByID(sectorID).get
    val event = sector.event.single
    val sale = Sale(0,event.id,new Timestamp(new Date().getTime()))
    salesTable.insert(sale)
    val seatstoInsert = (for {
      row <- sitsMap.zipWithIndex
      num <- row._1
    } yield Seat(0,sectorID,row._2+1,num,true,Some(sale.id)))
    Seat.insert(seatstoInsert)
  }
}