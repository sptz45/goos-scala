
lazy val root = (project in file("."))
  .configs(IntegrationTest)
  .settings(
    name := "goos-scala",
    version := "0.3.1",
    scalaVersion := "3.1.2",
    Defaults.itSettings,
    Test / parallelExecution := false,
    Test / fork := true,
    IntegrationTest / unmanagedBase := baseDirectory.value / "testlib",
    IntegrationTest / parallelExecution := false,
    IntegrationTest / fork := true,
    scalacOptions += "-deprecation",
    libraryDependencies ++= Seq(
      "commons-io"                  % "commons-io"         % "2.6",
      "org.apache.commons"          % "commons-lang3"      % "3.10",
      "org.igniterealtime.smack"    % "smack"              % "3.2.1",
      "org.igniterealtime.smack"    % "smackx"             % "3.2.1",
      "org.jmock"                   % "jmock"              % "2.12.0" % "it,test",
      "org.jmock"                   % "jmock-imposters"    % "2.12.0" % "it,test",
      "org.scalameta"              %% "munit"              % "0.7.29" % "it,test",

    ),
    testFrameworks += new TestFramework("munit.Framework"),
    addCommandAlias("compileAll", "compile;test:compile;it:compile"),
    addCommandAlias("testAll", "test;it:test"),
    addCommandAlias("build", "clean;compileAll;testAll")
  )
