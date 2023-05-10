// See README.md for license details.

ThisBuild / scalaVersion := "2.13.10"
ThisBuild / version := "0.1.0"
ThisBuild / organization := "name1e5s"

val chiselVersion = "3.6.0"

lazy val root = (project in file("."))
  .settings(
    name := "trinity",
    libraryDependencies ++= Seq(
      "edu.berkeley.cs" %% "chisel3" % chiselVersion,
      "edu.berkeley.cs" %% "chiseltest" % "0.6.0" % "test"
    ),
    scalacOptions ++= Seq(
      "-language:reflectiveCalls",
      "-language:implicitConversions",
      "-deprecation",
      "-feature",
      "-Xcheckinit"
    ),
    addCompilerPlugin(
      "edu.berkeley.cs" % "chisel3-plugin" % chiselVersion cross CrossVersion.full
    )
  )
