import sbt._
import Keys._

object HelloBuild extends Build {
  resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

  libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.3-SNAPSHOT"
}