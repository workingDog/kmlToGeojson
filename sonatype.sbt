
pomExtra := {
  <scm>
    <url>https://github.com/workingDog/kmlToGeojson</url>
    <connection>scm:git:git@github.com:workingDog/kmlToGeojson.git</connection>
  </scm>
    <developers>
      <developer>
        <id>workingDog</id>
        <name>Ringo Wathelet</name>
        <url>https://github.com/workingDog</url>
      </developer>
    </developers>
}

pomIncludeRepository := { _ => false }

// Release settings
sonatypeProfileName := "com.github.workingDog"
releasePublishArtifactsAction := PgpKeys.publishSigned.value
releaseCrossBuild := true
releaseTagName := (version in ThisBuild).value

import ReleaseTransformations._

releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  publishArtifacts,
  ReleaseStep(action = Command.process("publishSigned", _)),
  setNextVersion,
  commitNextVersion,
  ReleaseStep(action = Command.process("sonatypeReleaseAll", _)),
  pushChanges
)

