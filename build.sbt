name := "nest-dest"

organization := "com.daxzel"

version := "0.1-SNAPSHOT"

scalaVersion := "2.11.7"

val akkaV = "2.4.2"
val akkaStreamV = "2.0.3"

libraryDependencies ++= Seq(
  "com.typesafe.akka" % "akka-actor_2.11" % akkaV,
  "com.typesafe.akka" % "akka-stream-experimental_2.11" % akkaStreamV,
  "com.typesafe.akka" % "akka-http-core-experimental_2.11" % akkaStreamV,
  "com.typesafe.akka" % "akka-http-experimental_2.11" % akkaStreamV,
  "com.typesafe.akka" % "akka-http-spray-json-experimental_2.11" % akkaStreamV,
  "com.typesafe.akka" % "akka-http-testkit-experimental_2.11" % akkaStreamV,

  "com.firebase" % "firebase-client" % "1.0.18",
  "oauth.signpost" % "signpost-core" % "1.2.1.2",
  "org.codehaus.jackson" % "jackson-core-asl" % "1.9.13"
)

