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

  val Logger = new Logger(
    settings.numberWorkers,
    settings.epochs,
    settings.subsetPerWorker
  )

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
          if (isEarlyStoppingCriterionReached(losses)) (weights, losses)

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
    accuracy_val = svm.accuracy(validationSet, finalWeight)
  )

  sc.stop()

  def isEarlyStoppingCriterionReached(losses: List[Double]): Boolean =
    if (losses.size < 2 * settings.earlyStoppingWindow) false
    else {
      val recentWindow = losses.take(settings.earlyStoppingWindow)
      val oldWindow = losses
        .slice(settings.earlyStoppingWindow, 2 * settings.earlyStoppingWindow)

      val recentAverage = recentWindow.sum / recentWindow.size
      val oldAverage = oldWindow.sum / oldWindow.size

      oldAverage - recentAverage < settings.epsilon
    }
}
