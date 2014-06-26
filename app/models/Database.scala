package models

import org.squeryl.Schema
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.internals.AutoIncremented
import org.squeryl.dsl.OneToManyRelation

object Database extends Schema {
  val eventsTable = table[Event]("events")
  on(eventsTable)(e => declare(e.id is (autoIncremented)))
  val sectorsTable = table[Sector]("sectors")
  on (sectorsTable)(s=>declare(s.id is (autoIncremented)))
  val sitsTable = table[Seat]("sits")
  on (sitsTable)(s=>declare(s.id is (autoIncremented)))
  val salesTable = table[Sale]("sales")
  on (salesTable)(s=>declare(s.id is (autoIncremented)))
  
  val eventsToSectors = oneToManyRelation(eventsTable, sectorsTable).via(((e,s)=>e.id===s.eventID))
  val sectorsToSeats = oneToManyRelation(sectorsTable, sitsTable).via((sec,sit)=>sec.id===sit.sectorID)
  val eventsToSells = oneToManyRelation(eventsTable, salesTable).via((e,s)=>e.id===s.eventID)
  val salesToSeats = oneToManyRelation(salesTable, sitsTable).via((sale,sit)=>sale.id===sit.saleID)
}