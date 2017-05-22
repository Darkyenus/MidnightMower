import java.util

import com.darkyen.resourcepacker.{PackingOperation, PackingOperationKt}

name := "MidnightMower"

version := "0.1-SNAPSHOT"

organization := "com.darkyen"

crossPaths := false

autoScalaLibrary := false

val gdxVersion = "1.9.6"

baseDirectory in (Compile, run) := baseDirectory.value / "assets"

fork in run := true

resolvers += "jitpack" at "https://jitpack.io"

// Core
libraryDependencies ++= Seq(
  "com.badlogicgames.gdx" % "gdx" % gdxVersion,
  "org.slf4j" % "slf4j-api" % "1.7.22",
  "com.github.Darkyenus" % "tproll" % "v1.2.2"
)

//Desktop
libraryDependencies ++= Seq(
  "com.badlogicgames.gdx" % "gdx-backend-lwjgl3" % gdxVersion,
  "com.badlogicgames.gdx" % "gdx-platform" % gdxVersion classifier "natives-desktop"
)

javacOptions ++= Seq("-g", "-Xlint", "-Xlint:-rawtypes", "-Xlint:-unchecked")

javaOptions ++= Seq("-ea")

TaskKey[Unit]("packResources") := {
  ResourcePacker.resourcePack(new PackingOperation("./resources", "./assets", util.Arrays.asList(PackingOperationKt.getPreferSymlinks.to(true))))
}

fullClasspath in assembly += file("./assets")

TaskKey[Unit]("packResourcesDist") := {
  ResourcePacker.resourcePack(new PackingOperation("./resources", "./assets"))
}

mainClass in assembly := Some("com.darkyen.midnightmower.Main")
