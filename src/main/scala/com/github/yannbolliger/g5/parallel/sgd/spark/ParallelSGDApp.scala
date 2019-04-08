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

  val (finalWeight, validationLosses) =
    (1 to settings.epochs)
      .foldLeft((svm.initialWeights, List.empty[Double])) {

        case ((weights, losses), epoch) =>
          val lastLosses = losses.take(settings.earlyStoppingWindow)

          // if last losses are all equal we skip the iteration
          if (lastLosses.size > settings.earlyStoppingWindow &&
              lastLosses.forall(_ == lastLosses.head))
            (weights, losses)

          // otherwise calculate next epoch
          else {
            val newWeights = svm.fitEpoch(trainSet, weights)
            val validationLoss = svm.loss(validationSet, newWeights)

            Logger.appendLoss(validationLoss)
            println(s"Validation loss at epoch $epoch: $validationLoss")

            (newWeights, validationLoss :: losses)
          }
      }

  Logger.finish(
    accuracy_test = svm.accuracy(testData, finalWeight),
    accuracy_train = svm.accuracy(trainSet, finalWeight),
    accuracy_val = svm.accuracy(validationSet, finalWeight),
    losses_val = svm.loss(validationSet, finalWeight)
  )

  sc.stop()
}
