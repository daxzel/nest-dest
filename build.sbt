name := "nest-dest"

organization := "com.daxzel"

version := "0.1-SNAPSJHOT"

scalaVersion := "2.10.4"

libraryDependencies ++= Seq (
  "com.firebase" % "firebase-client" % "1.0.18",
  "com.typesafe.akka" %% "akka-actor" % "2.3.9",
  "oauth.signpost" % "signpost-core" % "1.2.1.2",
  "org.codehaus.jackson" % "jackson-core-asl" % "1.9.13"
)

