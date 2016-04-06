
name := "converter"

version := "0.1"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "org.scala-lang.modules" % "scala-xml_2.11" % "1.0.5",
  "com.typesafe.play" % "play-json_2.11" % "2.5.1",
  "com.typesafe.play.extras" % "play-geojson_2.11" % "1.4.0"
)

mainClass in (Compile, run) := Some("com.kodekutters.Converter")

mainClass in assembly := Some("com.kodekutters.Converter")

assemblyJarName in assembly := "converter-0.1.jar"