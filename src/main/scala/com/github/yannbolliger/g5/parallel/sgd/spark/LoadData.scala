package com.github.yannbolliger.g5.parallel.sgd.spark

import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD

object LoadData {

  private val trainDataPath =
    Settings.dataPath + "lyrl2004_vectors_train.dat"

  private val testDataPaths =
    Settings.dataPath + "lyrl2004_vectors_test_pt*.dat"

  private val topicsPath = Settings.dataPath + "rcv1-v2.topics.qrels"

  def load(
      sc: SparkContext
  ): (RDD[(Int, SparseVector, Boolean)], RDD[(Int, SparseVector, Boolean)]) = {

    println("Read data")

    val trainDataRaw = sc.textFile(trainDataPath)
    val testDataRaw = sc.textFile(testDataRaw)
    val topicsDataRaw = sc.textFile(topicsPath)

    val trainVectors = trainDataRaw.map(SparseVector.fromString(_))
    val testVectors = testDataRaw.map(SparseVector.fromString(_))

    // Parse topics to labels
    val topic = Settings.topicKey

    val idsLabels: RDD[(Int, Boolean)] = for (line <- topicsDataRaw)
      yield {
        val topicKey :: id :: _ = line.split(" ").toList

        (id.toInt, topicKey == topic)
      }

    def joinWithLabels(
        data: RDD[(Int, SparseVector)]
    ): RDD[(Int, SparseVector, Boolean)] = data.join(idsLabels).map {
      case (id, (vector, label)) => (id, vector, label)
    }

    (joinWithLabels(trainVectors), joinWithLabels(testVectors))
  }

}
