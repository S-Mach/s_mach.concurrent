scalaVersion := "2.11.1"

organization := "net.s_mach"

name := "concurrent"

version := "0.2-SNAPSHOT"

scalacOptions ++= Seq("-feature","-unchecked", "-deprecation")

libraryDependencies += "org.scalatest" % "scalatest_2.11" % "2.2.0" % "test"

//testOptions in Test += Tests.Argument("-l s_mach.concurrent.DelayAccuracyTest")

parallelExecution in Test := false

licenses += ("MIT", url("http://opensource.org/licenses/MIT"))