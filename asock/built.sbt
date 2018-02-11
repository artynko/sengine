name := "asock"

version := "1.0"

scalaVersion := "2.10.3"

  resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/snapshots/"

  libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.3-SNAPSHOT"

  libraryDependencies += "com.typesafe.akka" %% "akka-testkit" % "2.3-SNAPSHOT"