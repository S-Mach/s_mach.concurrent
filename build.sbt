scalaVersion := "2.11.1"

organization := "s_mach"

name := "concurrent"

version := "0.1-SNAPSHOT"

instrumentSettings

ScoverageKeys.minimumCoverage := 60

ScoverageKeys.failOnMinimumCoverage := true

ScoverageKeys.highlighting := true

coverallsSettings

scalacOptions ++= Seq("-feature","-unchecked", "-deprecation")

libraryDependencies += "org.scalatest" % "scalatest_2.11" % "2.2.0" % "test"

licenses += ("MIT", url("http://opensource.org/licenses/MIT"))

parallelExecution in Test := false

git.remoteRepo := "git@github.com:S-Mach/s_mach.concurrent.git"