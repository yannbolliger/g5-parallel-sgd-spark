package com.github.yannbolliger.g5.parallel.sgd.spark

import org.apache.spark.sql.SparkSession

import org.apache.log4j.Logger
import org.apache.log4j.Level

object ParallelSGDApp extends App {

  Logger.getLogger("org").setLevel(Level.OFF)
  Logger.getLogger("akka").setLevel(Level.OFF)

  var data_directory = ""
  if (args.length > 0) {
    data_directory = args(0)
  }
  else {
    data_directory = "TODO"
  }

  val spark =
    SparkSession.builder.appName("g5-parallel-sgd-spark").getOrCreate()

  val (train, test, topics) = LoadData.load(spark, data_directory)

  val trainData = train.map(SparseVector.fromString(_))

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

        // TODO: caluclate validation loss (KYLE)

        // TODO: log loss (KYLE)

        newWeights
      }
  }

  spark.stop()

}
