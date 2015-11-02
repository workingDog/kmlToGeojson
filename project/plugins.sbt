logLevel := Level.Warn

resolvers += "Bintray sbt plugin releases" at "http://dl.bintray.com/sbt/sbt-plugin-releases/"

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.0")