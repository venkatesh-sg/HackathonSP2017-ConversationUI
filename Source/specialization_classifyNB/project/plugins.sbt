logLevel := Level.Warn


resolvers += Resolver.sonatypeRepo("snapshots")

addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.0.3")
addSbtPlugin("com.earldouglas" %% "xsbt-web-plugin" % "3.0.1")
// https://mvnrepository.com/artifact/org.jbehave.web/jbehave-web-runner
//libraryDependencies += "org.jbehave.web" % "jbehave-web-runner" % "2.1.5"
