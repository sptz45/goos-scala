
lazy val root = (project in file("."))
  .configs(IntegrationTest)
  .settings(
    name := "goos-scala",
    version := "0.2.13",
    scalaVersion := "2.13.2",
    Defaults.itSettings,
    Test / parallelExecution := false,
    Test / fork := true,
    libraryDependencies ++= Seq(
      "commons-io"                  % "commons-io"         % "2.6",
      "org.apache.commons"          % "commons-lang3"      % "3.10",
      "org.igniterealtime.smack"    % "smack"              % "3.2.1",
      "org.igniterealtime.smack"    % "smackx"             % "3.2.1",
      "junit"                       % "junit"              % "4.11" % "it,test",
      "com.novocode"                % "junit-interface"    % "0.11" % "it,test",
      "org.jmock"                   % "jmock-junit4"       % "2.12.0" % "it,test",
      "com.googlecode.windowlicker" % "windowlicker-swing" % "r268" % "it,test"
    )
  )
