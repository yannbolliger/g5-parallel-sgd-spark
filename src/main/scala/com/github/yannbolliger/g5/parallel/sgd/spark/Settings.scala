package com.github.yannbolliger.g5.parallel.sgd.spark

object Settings {

  implicit def s2Bool: String => Boolean = _.toBoolean
  implicit def s2Int: String => Int = s => augmentString(s).toInt
  implicit def s2Double: String => Double = s => augmentString(s).toDouble

  def getFromEnvOrDefault[A](
      key: String,
      default: A
  )(implicit convert: String => A): A =
    sys.env.get(key).map(convert(_)).getOrElse(default)

  val dataPath = getFromEnvOrDefault("DATA_PATH", "resources")

  val numberWorkers: Int = getFromEnvOrDefault[Int]("N_WORKERS", 4)

  val envLocal: Boolean = getFromEnvOrDefault("RUN_LOCAL", true)

  val batchFraction: Double = getFromEnvOrDefault("BATCH_FRACTION", 0.1)

  val validationSplit: Double = getFromEnvOrDefault("VALIDATION_SPLIT", 0.1)

  val epochs: Int = getFromEnvOrDefault("EPOCHS", 1000)

  val learningRate: Double = getFromEnvOrDefault(
    "LEARNING_RATE",
    0.03 * batchFraction / numberWorkers
  )

  val lambda: Double = getFromEnvOrDefault("LAMBDA", 1E-5)
}
