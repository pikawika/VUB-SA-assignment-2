lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(
    name := """Session07""",
    version := "1.0",
    scalaVersion := "2.13.1",
    libraryDependencies ++= Seq(
      guice,
    ),
    scalacOptions ++= Seq(
      "-feature",
      "-deprecation",
      "-Xfatal-warnings"
    )
  )
