import sbt.Keys._

lazy val root = (project in file(".")).
  settings(

    name := "NaiveBayesModelGen",
    version := "1.0" ,
    scalaVersion := "2.11.8"

  )

exportJars := true
fork := true



val meta = """META.INF(.)*""".r

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "1.6.0" % "provided",
  "org.apache.spark" %% "spark-mllib" % "1.6.0",
  "edu.stanford.nlp" % "stanford-corenlp" % "3.6.0",
  "edu.stanford.nlp" % "stanford-corenlp" % "3.6.0" classifier "models",
  "edu.stanford.nlp" % "stanford-parser" % "3.6.0",
  "com.google.protobuf" % "protobuf-java" % "2.6.1"
)
