name := """play-scala-shopify-app"""
organization := "com.kylegalloway"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.8"

resolvers += "Atlassian Releases" at "https://maven.atlassian.com/public/"

libraryDependencies += filters
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test

// Silhouette for oauth
libraryDependencies ++= Seq(
  "com.mohiva" %% "play-silhouette" % "4.0.0",
  "com.mohiva" %% "play-silhouette-password-bcrypt" % "4.0.0",
  "com.mohiva" %% "play-silhouette-crypto-jca" % "4.0.0",
  "com.mohiva" %% "play-silhouette-persistence" % "4.0.0",
  "com.mohiva" %% "play-silhouette-testkit" % "4.0.0" % "test"
)

libraryDependencies += ws

// Scala Guice for Silhouette
libraryDependencies += "net.codingwell" %% "scala-guice" % "4.0.1"

// Ficus for config reading
libraryDependencies += "com.iheart" %% "ficus" % "1.4.0"

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.kylegalloway.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.kylegalloway.binders._"
