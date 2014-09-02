import com.typesafe.sbt.SbtGit._

resolvers += Classpaths.sbtPluginReleases

addSbtPlugin("org.scoverage" % "sbt-scoverage" % "0.99.7.1")

addSbtPlugin("org.scoverage" %% "sbt-coveralls" % "0.99.0")

git.remoteRepo := "git@github.com:S-Mach/s_mach.concurrent.git"