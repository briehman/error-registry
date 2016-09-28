lazy val root = (project in file(".")).
  settings(
    name := "hello",
    scalaVersion := "2.11.8"
  )

libraryDependencies ++= Seq(
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.8.2",
  "com.h2database" % "h2" % "1.3.175",
  "com.typesafe" % "config" % "1.3.0",
  "com.typesafe.slick" %% "slick" % "3.1.1",
  "com.zaxxer" % "HikariCP" % "2.4.1",
  "org.scalatest" %% "scalatest" % "3.0.0" % Test,
  "org.slf4j" % "slf4j-nop" % "1.6.4"
)
