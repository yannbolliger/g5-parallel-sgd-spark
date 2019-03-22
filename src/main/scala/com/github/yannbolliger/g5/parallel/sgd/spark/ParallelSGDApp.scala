package com.github.yannbolliger.g5.parallel.sgd.spark

import org.apache.spark.sql.SparkSession
import org.apache.spark.SparkContext
import org.apache.spark.SparkConf

import systems.LoadData

import org.apache.log4j.Logger
import org.apache.log4j.Level

object ParallelSGDApp extends App {

  override
  def main(args: Array[String]) {

    Logger.getLogger("org").setLevel(Level.OFF)
    Logger.getLogger("akka").setLevel(Level.OFF)


    var data_directory = ""
    if (args.length > 0) {
      data_directory = args(0)
    } else {
      data_directory = "TODO"
    }

    // val logFile = "text.txt"

    val loadData = new LoadData
    val spark = SparkSession.builder.appName("g5-parallel-sgd-spark").getOrCreate()

    val (train, test, topics) = loadData.load(spark, data_directory)

    println(test.count())
    println(train.count())
    println(topics.count())


    // val logData = spark.read.textFile(logFile).cache()

    // val numAs = logData.filter(line => line.contains("a")).count()
    // val numBs = logData.filter(line => line.contains("b")).count()

    // println(s"Lines with a: $numAs, Lines with b: $numBs")

    // spark.stop()
  }
}
