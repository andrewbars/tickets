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
  date: Timestamp,
  price: Int,
  confirmed: Boolean) extends KeyedEntity[Long] {
  lazy val event: ManyToOne[Event] =
    Database.eventsToSells.right(Sale.this)
  lazy val sits: OneToMany[Seat] =
    salesToSeats.left(Sale.this)
}

object Sale {
  def addNew(sitsMap: List[List[Int]], sectorID: Long, sale: Sale) = inTransaction {
    val sector = Sector.getByID(sectorID).get
    val event = sector.event.single
    salesTable.insert(sale)
    val seatstoInsert = (for {
      row <- sitsMap.zipWithIndex
      num <- row._1
    } yield Seat(0, sectorID, row._2 + 1, num, true,false, Some(sale.id),None))
    Seat.insert(seatstoInsert)
  }

  def saleByIdQ(saleID: Long) = salesTable.lookup(saleID)
  def getByID(saleID: Long) = inTransaction {
    saleByIdQ(saleID) match {
      case None => None
      case Some(sale) => Some(sale)
    }
  }
  def event(sale: Sale) = inTransaction {
    sale.event.single
  }
  def seats(sale: Sale) = inTransaction {
    sale.sits.toList
  }
  def deleteByEventID(eventID: Long) = inTransaction(salesTable.deleteWhere(_.eventID === eventID))
  def confirmSale(sale: Sale) = inTransaction(salesTable.update(sale.copy(confirmed = true)))
  def revertSale(sale: Sale) = inTransaction{
    if (!sale.confirmed) {
      sitsTable.delete(sale.sits)
      salesTable.deleteWhere(_.id === sale.id)
    }
  }
}