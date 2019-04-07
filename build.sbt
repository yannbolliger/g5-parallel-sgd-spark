name := "g5-parallel-sgd-spark"

version := "0.1"

scalaVersion := "2.11.12"


libraryDependencies += "org.apache.spark" %% "spark-sql" % "2.4.0"
libraryDependencies += "com.google.code.gson" % "gson" % "2.2.4"


artifactName := { (sv: ScalaVersion, module: ModuleID, artifact: Artifact) =>
  "jar-" + java.util.UUID.randomUUID.toString.take(6) + "." + artifact.extension
}