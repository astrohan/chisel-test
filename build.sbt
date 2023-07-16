//-------------------------------------------------
// Global setting
//-------------------------------------------------
val chiselVersion = "3.5.6"
val chiselTestVersion = "2.5.1"

lazy val chiselSettings = Seq(
  libraryDependencies ++= Seq(
    "edu.berkeley.cs" %% "chisel3" % chiselVersion,
    "org.apache.commons" % "commons-lang3" % "3.12.0",
    "org.apache.commons" % "commons-text" % "1.9"),
  addCompilerPlugin("edu.berkeley.cs" % "chisel3-plugin" % chiselVersion cross CrossVersion.full)
)

lazy val chiselTestSettings = Seq(libraryDependencies ++= Seq("edu.berkeley.cs" %% "chisel-iotesters" % chiselTestVersion))

def freshProject(name: String, dir: File): Project = {
  Project(id = name, base = dir / "src")
    .settings(
      Compile / scalaSource := baseDirectory.value / "main" / "scala",
      Compile / resourceDirectory := baseDirectory.value / "main" / "resources"
    )
}

lazy val commonSettings = Seq(
  scalaVersion := "2.13.10",
  scalacOptions ++= Seq("-deprecation", "-unchecked"),
  unmanagedBase := (testRoot / unmanagedBase).value,
  allDependencies := {
    // drop specific maven dependencies in subprojects
    val dropDeps = Seq(("edu.berkeley.cs", "rocketchip"))
    allDependencies.value.filterNot { dep =>
      dropDeps.contains((dep.organization, dep.name))
    }
  },
  exportJars := true,
)
//-------------------------------------------------
// ~Global setting
//-------------------------------------------------


//-------------------------------------------------
// Rocket-chip dependencies (subsumes making RC a RootProject)
//-------------------------------------------------
val rocketChipDir = file("generators/rocket-chip")

lazy val cde = (project in file("tools/cde"))
  .settings(commonSettings)
  .settings(Compile / scalaSource := baseDirectory.value / "cde/src/chipsalliance/rocketchip")

lazy val hardfloat  = (project in rocketChipDir / "hardfloat")
  .settings(chiselSettings)
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      "org.scala-lang" % "scala-reflect" % scalaVersion.value,
      "org.json4s" %% "json4s-jackson" % "3.6.6",
      "org.scalatest" %% "scalatest" % "3.2.0" % "test"
    )
  )

lazy val rocketMacros  = (project in rocketChipDir / "macros")
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      "org.scala-lang" % "scala-reflect" % scalaVersion.value,
      "org.json4s" %% "json4s-jackson" % "3.6.6",
      "org.scalatest" %% "scalatest" % "3.2.0" % "test"
    )
  )

lazy val rocketchip = freshProject("rocketchip", rocketChipDir)
  .dependsOn(hardfloat, rocketMacros, cde)
  .settings(chiselSettings, commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      "org.scala-lang" % "scala-reflect" % scalaVersion.value,
      "org.json4s" %% "json4s-jackson" % "3.6.6",
      "org.scalatest" %% "scalatest" % "3.2.0" % "test"
    )
  )

lazy val rocketLibDeps = (rocketchip / Keys.libraryDependencies)
//-------------------------------------------------
// ~Rocket-chip dependencies (subsumes making RC a RootProject)
//-------------------------------------------------




lazy val nvdla = (project in file("generators/nvdla"))
  .dependsOn(rocketchip)
  .settings(libraryDependencies ++= rocketLibDeps.value)
  .settings(commonSettings)



//-------------------------------------------------
// my project
//-------------------------------------------------
// This gives us a nicer handle to the root project instead of using the implicit one
lazy val testRoot = Project("testRoot", file("."))

lazy val roma = (project in file("tools/roma"))
  .dependsOn(rocketchip)
  .settings(libraryDependencies ++= rocketLibDeps.value)
  .settings(commonSettings)

lazy val test = (project in file("generators/test"))
  .dependsOn(rocketchip, nvdla)
  .settings(libraryDependencies ++= rocketLibDeps.value)
  .settings(chiselSettings, commonSettings)
  .settings(name := "test")
  .settings(scalacOptions ++= Seq(
    "-language:reflectiveCalls",
    "-deprecation",
    "-feature",
    "-Xcheckinit",
    "-P:chiselplugin:genBundleElements")
  )
//-------------------------------------------------
// ~root project
//-------------------------------------------------
