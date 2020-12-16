lazy val library = Project(AppDependencies.appName, file("."))
  .enablePlugins(SbtAutoBuildPlugin, SbtGitVersioning, SbtArtifactory)
  .settings(majorVersion := 2)
  .settings(
    scalaVersion := "2.11.11",
    libraryDependencies ++= AppDependencies(),
    crossScalaVersions := Seq("2.11.11"),
    parallelExecution in Test := false,
    makePublicallyAvailableOnBintray := true
  )
