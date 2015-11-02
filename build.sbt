
name := "converter"

version := "0.1"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "org.scala-lang.modules" % "scala-xml_2.11" % "1.0.5",
  "com.typesafe.play" % "play-json_2.11" % "2.5.0-M1"
//  "com.typesafe.play.extras" %% "play-geojson" % "1.3.1-SNAPSHOT"
)

mainClass in (Compile, run) := Some("com.kodekutters.Converter")

mainClass in assembly := Some("com.kodekutters.Converter")

assemblyJarName in assembly := "converter-0.1.jar"