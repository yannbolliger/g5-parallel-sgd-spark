package com.github.yannbolliger.g5.parallel.sgd.spark

import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD

object DataHelper {

  def load(
      sc: SparkContext
  ): (RDD[(Int, SparseVector, Boolean)], RDD[(Int, SparseVector, Boolean)]) = {

    val trainDataRaw = sc.textFile(Settings.trainFileName)
    val testDataRaw = sc.textFile(Settings.testFileNames)
    val topicsDataRaw = sc.textFile(Settings.topicsFileName)

    val trainVectors = trainDataRaw.map(SparseVector.fromString)
    val testVectors = testDataRaw.map(SparseVector.fromString)

    val idsLabels: RDD[(Int, Boolean)] = for (line <- topicsDataRaw)
      yield {
        val topic :: id :: _ = line.split(" ").toList

        (id.toInt, topic == Settings.topicKey)
      }

    def joinWithLabels(
        data: RDD[(Int, SparseVector)]
    ): RDD[(Int, SparseVector, Boolean)] = data.join(idsLabels).map {
      case (id, (vector, label)) => (id, vector, label)
    }

    (joinWithLabels(trainVectors), joinWithLabels(testVectors))
  }

  def trainValidationSplit(
      data: RDD[(Int, SparseVector, Boolean)]
  ): (RDD[(Int, SparseVector, Boolean)], RDD[(Int, SparseVector, Boolean)]) = {

    val splitWeights = Array(
      Settings.validationSplit,
      1 - Settings.validationSplit
    )

    val Array(train, validation) = data.randomSplit(splitWeights)

    (train, validation)
  }
}
