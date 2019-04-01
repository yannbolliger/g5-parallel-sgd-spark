package com.github.yannbolliger.g5.parallel.sgd.spark

import org.apache.spark.SparkContext
import org.apache.spark.SparkConf

object ParallelSGDApp extends App {

  val sparkConf = new SparkConf().setAppName("g5-parallel-sgd-spark")
  val sc = new SparkContext(sparkConf)

  val (trainData, testData) = DataHelper.load(sc)
  val (trainSet, validationSet) = DataHelper.trainValidationSplit(trainData)

  val svm = new SVM(
    Settings.learningRate,
    Settings.lambda,
    Settings.batchFraction,
    Settings.dimension
  )

  val finalWeight = (1 to Settings.epochs).foldLeft(svm.initialWeights) {
    (weights, epoch) =>
      {
        val newWeights = svm.fitEpoch(trainData, weights)

        val validationLoss = svm.loss(validationSet, weights)

        // TODO: log loss (KYLE)

        newWeights
      }
  }

  sc.stop()
}
