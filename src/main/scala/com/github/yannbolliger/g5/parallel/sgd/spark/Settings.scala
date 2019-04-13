package com.github.yannbolliger.g5.parallel.sgd.spark

import org.apache.spark.{HashPartitioner, Partitioner, SparkContext}

class Settings(sc: SparkContext, args: Array[String]) extends Serializable {

  implicit def s2Bool: String => Boolean = _.toBoolean

  implicit def s2Int: String => Int = s => augmentString(s).toInt

  implicit def s2Double: String => Double = s => augmentString(s).toDouble

  def getFromEnvOrDefault[A](
      key: String,
      default: A
  )(implicit convert: String => A): A =
    sys.env.get(key).map(convert).getOrElse(default)

  /**
    * Spark, system parameters
    */
  val numberWorkers: Int = sc.getConf.getInt("spark.executor.instances", 4)

  val partitioner: Partitioner = new HashPartitioner(2 * numberWorkers)

  val envLocal: Boolean = getFromEnvOrDefault("RUN_LOCAL", true)

  /**
    * Data parameters
    */
  val dataPath: String = getFromEnvOrDefault("DATA_PATH", "resources/rcv1")

  val logPath: String =
    if (sys.env.get("DATA_PATH").isDefined) "/data/logs"
    else "resources/logs"

  val trainFileName: String = dataPath + "/lyrl2004_vectors_train.dat"
  val testFileNames: String = dataPath + "/lyrl2004_vectors_test_pt*.dat"
  val topicsFileName: String = dataPath + "/rcv1-v2.topics.qrels"

  val topicKey: String = getFromEnvOrDefault("TOPIC_KEY", "CCAT")

  val dimension: Int = 47236
  val trainSize: Int = 23149

  /**
    * SGD parameters
    */
  val subsetPerWorker: Int = args(0)

  val batchSize: Double = subsetPerWorker * numberWorkers

  val batchFraction: Double = Math.min(1, batchSize / trainSize)

  val validationSplit: Double = getFromEnvOrDefault("VALIDATION_SPLIT", 0.1)

  val epochs: Int = args(1)

  val learningRate: Double = getFromEnvOrDefault(
    "LEARNING_RATE",
    0.3 * Math.max(1, Math.min(3, batchSize / 500))
  )
  val lambda: Double = getFromEnvOrDefault("LAMBDA", 1E-5)

  val earlyStoppingWindow: Int = 20

  val epsilon: Double = 0.01
}
