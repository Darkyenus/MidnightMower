logLevel := Level.Warn

resolvers += "jitpack" at "https://jitpack.io"

libraryDependencies += "com.github.Darkyenus" % "resourcepacker" % "2.1-SNAPSHOT"
//libraryDependencies += "com.github.Darkyenus" % "ResourcePacker" % "2.0"

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.13.0")
