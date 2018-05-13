organization := "com.github.workingDog"

name := "kmlToGeojson"

version := (version in ThisBuild).value

scalaVersion := "2.12.6"

crossScalaVersions := Seq("2.12.6")

resolvers += Resolver.bintrayRepo("jroper", "maven")

libraryDependencies ++= Seq(
  "org.scala-lang.modules" %% "scala-xml" % "1.1.0",
  "com.typesafe.play" %% "play-json" % "2.6.9",
  "au.id.jazzy" %% "play-geojson" % "1.5.0",
  "com.github.workingDog" %% "scalakml" % "1.4"
)

homepage := Some(url("https://github.com/workingDog/kmlToGeojson"))

licenses := Seq("Apache 2" -> url("http://www.apache.org/licenses/LICENSE-2.0"))

mainClass in(Compile, run) := Some("com.kodekutters.KmlToGeojson")

test in assembly := {}

assemblyMergeStrategy in assembly := {
  case PathList(xs@_*) if xs.last.toLowerCase endsWith ".rsa" => MergeStrategy.discard
  case PathList(xs@_*) if xs.last.toLowerCase endsWith ".dsa" => MergeStrategy.discard
  case PathList(xs@_*) if xs.last.toLowerCase endsWith ".sf" => MergeStrategy.discard
  case PathList(xs@_*) if xs.last.toLowerCase endsWith ".des" => MergeStrategy.discard
  case PathList(xs@_*) if xs.last endsWith "LICENSES.txt" => MergeStrategy.discard
  case PathList(xs@_*) if xs.last endsWith "LICENSE.txt" => MergeStrategy.discard
  case PathList(xs@_*) if xs.last endsWith "logback.xml" => MergeStrategy.discard
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}

mainClass in assembly := Some("com.kodekutters.KmlToGeojson")

assemblyJarName in assembly := "kmltogeojson-" + version.value + ".jar"