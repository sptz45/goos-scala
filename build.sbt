
lazy val root = (project in file("."))
  .configs(IntegrationTest)
  .settings(
    name := "goos-scala",
    version := "0.2.13",
    scalaVersion := "2.13.1",
    Defaults.itSettings,
    Test / parallelExecution := false,
    Test / fork := true,
    IntegrationTest / unmanagedBase := baseDirectory.value / "testlib",
    IntegrationTest / parallelExecution := false,
    IntegrationTest / fork := true,
    inConfig(IntegrationTest)(scalafixConfigSettings(IntegrationTest)),
    addCompilerPlugin(scalafixSemanticdb), // enable SemanticDB
    semanticdbEnabled := true, // enable SemanticDB
    semanticdbVersion := scalafixSemanticdb.revision, // use Scalafix compatible version
    scalacOptions += "-Ywarn-unused:imports", // required by `RemoveUnused` rule
    scalacOptions += "-Yrangepos",          // required by SemanticDB compiler plugin
    scalacOptions += "-deprecation",
    libraryDependencies ++= Seq(
      "commons-io"                  % "commons-io"         % "2.6",
      "org.apache.commons"          % "commons-lang3"      % "3.10",
      "org.igniterealtime.smack"    % "smack"              % "3.2.1",
      "org.igniterealtime.smack"    % "smackx"             % "3.2.1",
      "junit"                       % "junit"              % "4.11"   % "it,test",
      "com.novocode"                % "junit-interface"    % "0.11"   % "it,test",
      "org.jmock"                   % "jmock-junit4"       % "2.12.0" % "it,test"
    ),
    addCommandAlias("compileAll", "compile;test:compile;it:compile"),
    addCommandAlias("testAll", "test;it:test"),
    addCommandAlias("build", "clean;compileAll;testAll")
  )
