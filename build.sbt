lazy val scalatraVersion = "2.3.1"

scalacOptions  ++= Seq("-feature", "-language:postfixOps")

lazy val root = (project in file("."))
  .enablePlugins(JettyPlugin)
  .settings(
    organization := "com.briehman",
    name := "error-registry",
    version := "0.1-SNAPSHOT",
    scalaVersion := "2.11.8",
    libraryDependencies ++= Seq(
      "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.8.2",
      "com.github.sstone" % "amqp-client_2.11" % "1.5",
      "com.h2database" % "h2" % "1.3.175",
      "com.typesafe" % "config" % "1.3.0",
      "com.typesafe.akka" %% "akka-actor" % "2.4.0",
      "com.typesafe.slick" %% "slick" % "3.1.1",
      "com.zaxxer" % "HikariCP" % "2.4.1",
      "javax.servlet" % "javax.servlet-api" % "3.1.0" % "provided",
      "mysql" % "mysql-connector-java" % "5.1.32",
      "org.eclipse.jetty" % "jetty-webapp" % "9.2.10.v20150310" % "container",
      "org.json4s" %% "json4s-jackson" % "3.3.0",
      "org.mockito" % "mockito-core" % "1.10.19" % Test,
      "org.scala-lang" % "scala-compiler" % scalaVersion.value,
      "org.scala-lang" % "scala-library" % scalaVersion.value,
      "org.scala-lang" % "scala-reflect" % scalaVersion.value,
      "org.scala-lang.modules" % "scala-xml_2.11" % "1.0.5",
      "org.scalatest" %% "scalatest" % "3.0.0" % Test,
      "org.scalatra" %% "scalatra" % scalatraVersion,
      "org.scalatra" %% "scalatra-json" % scalatraVersion,
      "org.scalatra" %% "scalatra-scalate" % scalatraVersion,
      "org.scalatra" %% "scalatra-specs2" % scalatraVersion % "test",
      "org.slf4j" % "slf4j-nop" % "1.6.4",
      "org.webjars" % "datatables" % "1.10.13",
      "org.webjars" % "jquery" % "3.1.1-1"
    )
  )


javaOptions in Jetty ++= Seq(
  "-Xdebug",
  "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"
)

