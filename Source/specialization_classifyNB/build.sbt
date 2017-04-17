lazy val root = (project in file(".")).enablePlugins(WebappPlugin).enablePlugins(WarPlugin).
  settings(
    name := "specialist",
    version := "1.0",
    scalaVersion := "2.11.6"
  )
javaOptions += "-Xmx475m"
libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "1.6.0" % "provided",
  "org.apache.spark" %% "spark-mllib" % "1.6.0",
  "javax.servlet" % "javax.servlet-api" % "3.0.1" % "provided",
  "org.apache.httpcomponents" % "httpcore" % "4.4.5",
  "org.apache.httpcomponents" % "httpclient" % "4.5.2"
)
