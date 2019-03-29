package com.github.yannbolliger.g5.parallel.sgd.spark

import org.apache.spark.SparkContext
import org.apache.spark.SparkConf

import org.apache.log4j.Logger
import org.apache.log4j.Level

object ParallelSGDApp extends App {

  Logger.getLogger("org").setLevel(Level.OFF)
  Logger.getLogger("akka").setLevel(Level.OFF)

  val sparkConf = new SparkConf().setAppName("g5-parallel-sgd-spark")
  val sc = new SparkContext(sparkConf)

  val (trainData, testData) = LoadData.load(sc)

  // TODO: split data to val and train (YANN)

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

        // TODO: caluclate validation loss (YANN)

        // TODO: log loss (KYLE)

        newWeights
      }
  }

  sc.stop()

}
