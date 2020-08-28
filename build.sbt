import sbtassembly.AssemblyPlugin.defaultUniversalScript

lazy val root = (project in file("."))
  .settings(
    name := "snowflake-to-stdout",
    organization := "mwrobel.com",
    version := "1.0.0",
    scalacOptions := Seq("-unchecked", "-deprecation", "-feature"),
    libraryDependencies ++= Seq(
        // useful new types like OR
        "com.github.scopt" %% "scopt" % "3.7.1",
        // snowflake
        "net.snowflake" % "snowflake-jdbc" % "3.12.5",
        // logging
        "ch.qos.logback"             % "logback-classic" % "1.2.3",
        "com.typesafe.scala-logging" %% "scala-logging"  % "3.9.2",
        // tests
        "org.scalatest" %% "scalatest" % "3.1.0" % "test"
      ),
    scalaVersion := "2.12.10",
    scalafmtOnCompile := true,
    Test / parallelExecution := false,
    // needed for assembling a fat jar
    assembly / mainClass := Some("com.mwrobel.unloader.Main"),
    assembly / assemblyJarName := "snowflake-to-stdout",
    assembly / test := {},
    assembly / target := file("target/jars"),
    assembly / assemblyOption := (assemblyOption in assembly).value.copy(
        prependShellScript = Some(defaultUniversalScript(shebang = false))
      )
//    assembly / assemblyJarName := s"${name.value}-${version.value}"
  )
