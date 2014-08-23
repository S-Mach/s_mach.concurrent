scalaVersion := "2.11.0"

organization := "s_mach"

name := "concurrent"

version := "0.1-SNAPSHOT"

scalacOptions ++= Seq("-feature","-unchecked", "-deprecation")

libraryDependencies += "org.scalatest" % "scalatest_2.11" % "2.2.0" % "test"

parallelExecution in Test := false