package com.github.yannbolliger.g5.parallel.sgd.spark

import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD

object DataHelper {

  type LabelledData = (Int, (SparseVector, Boolean))

  def load(sc: SparkContext): (RDD[LabelledData], RDD[LabelledData]) = {

    val fileNames = List(Settings.trainFileName, Settings.testFileNames)
    val topicsDataRaw = sc.textFile(Settings.topicsFileName)

    val idsLabels: RDD[(Int, Boolean)] = (for {
      line <- topicsDataRaw
    } yield {
      val topic :: id :: _ = line.split(" ").toList

      (id.toInt, topic == Settings.topicKey)
    }).groupByKey()
      .mapValues(_.exists(t => t))

    val trainData :: testData :: _ = fileNames.map(
      fileName =>
        sc.textFile(fileName)
          .map(SparseVector.fromString)
          .partitionBy(Settings.partitioner)
          .join(idsLabels)
    )

    (trainData, testData.persist)
  }

  def trainValidationSplit(
      data: RDD[LabelledData]
  ): (RDD[LabelledData], RDD[LabelledData]) = {

    val splitWeights = Array(
      1 - Settings.validationSplit,
      Settings.validationSplit
    )

    val Array(train, validation) = data.randomSplit(splitWeights)

    (train.persist, validation.persist)
  }
}
