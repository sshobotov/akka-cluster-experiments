name := "akka-cluster-experiments"

version := "0.1.0"

scalaVersion := "2.12.2"

scalacOptions ++= Seq("-feature", "-unchecked", "-deprecation")

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor"             % "2.5.4",
  "com.typesafe.akka" %% "akka-persistence"       % "2.5.4",
  "com.typesafe.akka" %% "akka-cluster"           % "2.5.4",
  "com.typesafe.akka" %% "akka-cluster-sharding"  % "2.5.4"
)
        