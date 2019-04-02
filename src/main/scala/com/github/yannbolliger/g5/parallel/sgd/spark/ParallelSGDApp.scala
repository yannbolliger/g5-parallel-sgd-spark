package com.github.yannbolliger.g5.parallel.sgd.spark

import org.apache.spark.SparkContext
import org.apache.spark.SparkConf

object ParallelSGDApp extends App {

  val sparkConf = new SparkConf().setAppName("g5-parallel-sgd-spark")
  val sc = new SparkContext(sparkConf)

  val (trainData, testData) = DataHelper.load(sc)
  val (trainSet, validationSet) = DataHelper.trainValidationSplit(trainData)

  print(trainSet.toDebugString)

  val svm = new SVM(
    Settings.learningRate,
    Settings.lambda,
    Settings.batchFraction,
    Settings.dimension
  )

  val Logger = new Logger(Settings.numberWorkers, Settings.epochs)

  val finalWeight = (1 to Settings.epochs).foldLeft(svm.initialWeights) {
    (weights, epoch) =>
      {
        val newWeights = svm.fitEpoch(trainSet, weights)

        val validationLoss = svm.loss(validationSet, weights)

        Logger.appendLoss(validationLoss)

        newWeights
      }
  }


  Logger.finish()


  sc.stop()
}
