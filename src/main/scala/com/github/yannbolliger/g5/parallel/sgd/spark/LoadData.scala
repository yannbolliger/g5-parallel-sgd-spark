package com.github.yannbolliger.g5.parallel.sgd.spark

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession

// val spark = SparkSession.builder.appName("Simple Application").getOrCreate()

// val conf = new SparkConf().setAppName("g5-parallel-sgd-spark").setMaster("local")
// new SparkContext(conf)

object LoadData {

  def load(
      spark: SparkSession,
      data_directory: String
  ): (RDD[String], RDD[String], RDD[String]) = {

    println("Return data ...")

    val test_files = s"${data_directory}lyrl2004_vectors_test_pt0.dat.gz"
    val test = spark.read.textFile(test_files)

    val train =
      spark.read.textFile(data_directory + "lyrl2004_vectors_train.dat.gz")
    val topics = spark.read.textFile(data_directory + "rcv1-v2.topics.qrels.gz")

    // TODO: take care of labels here (YANN)

    (test, train, topics)
  }

}
