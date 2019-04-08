package com.github.yannbolliger.g5.parallel.sgd.spark

import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD

object DataHelper {

  type LabelledData = (Int, (SparseVector, Boolean))

  def load(
      sc: SparkContext,
      settings: Settings
  ): (RDD[LabelledData], RDD[LabelledData]) = {

    val fileNames = List(settings.trainFileName, settings.testFileNames)
    val topicsDataRaw = sc.textFile(settings.topicsFileName)

    val idsLabels: RDD[(Int, Boolean)] = topicsDataRaw
      .map(line => {
        val topic :: id :: _ = line.split(" ").toList

        (id.toInt, topic == settings.topicKey)
      })
      .reduceByKey(_ || _)
      .persist

    val trainData :: testData :: _ = fileNames.map(
      fileName =>
        sc.textFile(fileName)
          .map(SparseVector.fromStringWithBias)
          .partitionBy(settings.partitioner)
          .join(idsLabels)
    )

    (trainData.persist, testData.persist)
  }

  def trainValidationSplit(
      data: RDD[LabelledData],
      settings: Settings
  ): (RDD[LabelledData], RDD[LabelledData]) = {

    val splitWeights = Array(
      1 - settings.validationSplit,
      settings.validationSplit
    )

    val Array(train, validation) = data.randomSplit(splitWeights)
    (train.persist, validation.persist)
  }
}
