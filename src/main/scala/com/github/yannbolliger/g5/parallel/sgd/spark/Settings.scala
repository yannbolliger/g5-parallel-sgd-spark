package com.github.yannbolliger.g5.parallel.sgd.spark
import org.apache.spark.{HashPartitioner, Partitioner}

object Settings {

  implicit def s2Bool: String => Boolean = _.toBoolean
  implicit def s2Int: String => Int = s => augmentString(s).toInt
  implicit def s2Double: String => Double = s => augmentString(s).toDouble

  def getFromEnvOrDefault[A](
      key: String,
      default: A
  )(implicit convert: String => A): A =
    sys.env.get(key).map(convert(_)).getOrElse(default)

  /**
    * Spark, system parameters
    */
  val numberWorkers: Int = getFromEnvOrDefault("N_WORKERS", 4)

  val partitioner: Partitioner = new HashPartitioner(2 * numberWorkers)

  val envLocal: Boolean = getFromEnvOrDefault("RUN_LOCAL", true)

  /**
    * Data parameters
    */
  val dataPath: String = getFromEnvOrDefault("DATA_PATH", "resources/rcv1")

  val trainFileName: String = dataPath + "/lyrl2004_vectors_train.dat"
  val testFileNames: String = dataPath + "/lyrl2004_vectors_test_pt*.dat"
  val topicsFileName: String = dataPath + "/rcv1-v2.topics.qrels"

  val topicKey: String = getFromEnvOrDefault("TOPIC_KEY", "CCAT")

  val dimension: Int = 47237

  /**
    * SGD parameters
    */
  val batchFraction: Double = getFromEnvOrDefault("BATCH_FRACTION", 0.1)

  val validationSplit: Double = getFromEnvOrDefault("VALIDATION_SPLIT", 0.1)

  val epochs: Int = getFromEnvOrDefault("EPOCHS", 1000)

  val learningRate: Double = getFromEnvOrDefault(
    "LEARNING_RATE",
    3 * batchFraction / numberWorkers
  )

  val lambda: Double = getFromEnvOrDefault("LAMBDA", 1E-5)
}
