package models

import org.squeryl.Schema
import org.squeryl.PrimitiveTypeMode._

object Database extends Schema {
  val eventsTable = table[Event]("events")
  on(eventsTable)(e => declare(e.id is (autoIncremented)))
}