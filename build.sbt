name := "concurrent"

//testOptions in Test += Tests.Argument("-l s_mach.concurrent.DelayAccuracyTest")

parallelExecution in Test := false

libraryDependencies ++= Seq(
  "net.s_mach" %% "codetools" % "2.1.0"
)