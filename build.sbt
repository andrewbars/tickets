name := "tickets"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache
)     

libraryDependencies +="mysql" % "mysql-connector-java" % "5.1.18"

libraryDependencies +="org.squeryl" %% "squeryl" %"0.9.5-6"

libraryDependencies ++= Seq(
	"jp.t2v" %% "play2-auth"      % "0.11.0",
	"jp.t2v" %% "play2-auth-test" % "0.11.0" % "test"
)

play.Project.playScalaSettings
