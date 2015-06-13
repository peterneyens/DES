name := "des-scala"

version := "0.1"

scalaVersion := "2.11.6"

val akkaVersion = "2.3.11"
val akkaStreamVersion = "1.0-RC3"

libraryDependencies += "junit" % "junit" % "4.11"

libraryDependencies += "com.novocode" % "junit-interface" % "0.8" % "test->default"

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % akkaVersion

libraryDependencies += "com.typesafe.akka" %% "akka-cluster" % akkaVersion

libraryDependencies += "com.typesafe.akka" %% "akka-contrib" % akkaVersion

libraryDependencies +="com.typesafe.akka" %% "akka-stream-experimental" % akkaStreamVersion
