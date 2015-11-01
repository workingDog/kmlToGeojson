name := "kmltogeojson"

version := "1.0"

scalaVersion := "2.11.7"

resolvers += Resolver.sonatypeRepo("snapshots")

libraryDependencies ++= Seq(
  "org.scala-lang.modules" % "scala-xml_2.11" % "1.0.5",
  "com.typesafe.play" % "play-json_2.11" % "2.5.0-M1"
//  "com.typesafe.play.extras" %% "play-geojson" % "1.3.1-SNAPSHOT"
)

