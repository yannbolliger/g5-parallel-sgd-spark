package com.github.yannbolliger.g5.parallel.sgd.spark

import org.apache.spark.SparkContext
import org.apache.spark.SparkConf

object ParallelSGDApp extends App {

  val sparkConf = new SparkConf().setAppName("g5-parallel-sgd-spark")
  val sc = new SparkContext(sparkConf)
  sc.setLogLevel("OFF")

  val settings = new Settings(sc, args)

  val (trainData, testData) = DataHelper.load(sc, settings)

  val (trainSet, validationSet) =
    DataHelper.trainValidationSplit(trainData, settings)

  testData.persist
  trainSet.persist
  validationSet.persist

  val Logger = new Logger(settings.numberWorkers, settings.epochs)

  val svm = new SVM(
    settings.learningRate,
    settings.lambda,
    settings.batchFraction,
    settings.dimension
  )

  // TODO: early stopping

  val finalWeight = (1 to settings.epochs).foldLeft(svm.initialWeights) {
    (weights, epoch) =>
      {
        val newWeights = svm.fitEpoch(trainSet, weights)

        val validationLoss = svm.loss(validationSet, newWeights)

        Logger.appendLoss(validationLoss)

        println(s"======================\n\n$validationLoss\n\n===============")
        newWeights
      }
  }

  // Statistics
  val accuracy_test = svm.acc(testData, finalWeight)
  val accuracy_train = svm.acc(trainSet, finalWeight)
  val accuracy_val = svm.acc(validationSet, finalWeight)
  val losses_val = svm.loss(validationSet, finalWeight)

  Logger.finish(
    accuracy_test=accuracy_test,
    accuracy_train=accuracy_train,
    accuracy_val=accuracy_val,
    losses_val=losses_val
  )

  sc.stop()
}
