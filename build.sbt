organization := "com.github.workingDog"

name := "kmlToGeojson"

version := (version in ThisBuild).value

scalaVersion := "2.11.8"

crossScalaVersions := Seq("2.11.8")

libraryDependencies ++= Seq(
  "org.scala-lang.modules" % "scala-xml_2.11" % "1.0.6",
  "com.typesafe.play" % "play-json_2.11" % "2.5.9",
  "com.typesafe.play.extras" % "play-geojson_2.11" % "1.4.0",
  "com.github.workingDog" % "scalakml_2.11" % "1.0"
)

homepage := Some(url("https://github.com/workingDog/kmlToGeojson"))

licenses := Seq("Apache 2" -> url("http://www.apache.org/licenses/LICENSE-2.0"))

mainClass in (Compile, run) := Some("com.kodekutters.KmlToGeojson")

mainClass in assembly := Some("com.kodekutters.KmlToGeojson")

assemblyJarName in assembly := "kmltogeojson-1.0.jar"
