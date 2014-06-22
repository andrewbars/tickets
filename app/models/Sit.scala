package models

import org.squeryl.KeyedEntity
import java.sql.Timestamp
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.Table
import org.squeryl.Query
import org.squeryl.dsl.{ OneToMany, ManyToOne }
import collection.Iterable

case class Sector(
  val id: Long,
  val name: String,
  val numOfSeats: Int,
  val sitPrice: Int,
  val eventID: Long) extends KeyedEntity[Long] {
  lazy val event: ManyToOne[Event] =
    Database.eventsToSectors.right(this)
  lazy val sits: OneToMany[Sit] =
    Database.sectorsToSeats.left(this)
}

case class Sit(
  val id: Long,
  val sectorID: Long,
  val rowNumber: Int,
  val num: Int,
  val sold: Boolean) extends KeyedEntity[Long] {
  lazy val sector: ManyToOne[Sector] =
    Database.sectorsToSeats.right(this)
}

object Sector {
  def insert(sectors: List[Sector]) = {
    inTransaction {
      Database.sectorsTable.insert(sectors)
      val sits = for {
        s <- sectors
        r <- if (s.numOfSeats == 1000) (1 to 20) else (1 to 10)
        n <- if (s.numOfSeats == 1000) (1 to 50) else (1 to 20)
      } yield Sit(0, s.id, r, n, false)
      Sit.insert(sits)
    }
  }
}

object Sit {
  def insert(sits: List[Sit]) = inTransaction(Database.sitsTable.insert(sits))
}