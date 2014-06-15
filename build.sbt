name := "tickets"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache
)     

libraryDependencies +="mysql" % "mysql-connector-java" % "5.1.18"

libraryDependencies +="org.squeryl" %% "squeryl" %"0.9.5-6"

play.Project.playScalaSettings
