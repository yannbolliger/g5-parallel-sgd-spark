package systems

import org.apache.spark.sql.SparkSession
import org.apache.spark.SparkContext
import org.apache.spark.SparkConf
import org.apache.spark.sql.Dataset

// val spark = SparkSession.builder.appName("Simple Application").getOrCreate()

// val conf = new SparkConf().setAppName("g5-parallel-sgd-spark").setMaster("local")
// new SparkContext(conf)


class LoadData {

  def load(spark: org.apache.spark.sql.SparkSession, data_directory : String) = {
    println("Return data ...")

    // format :: RDD[(Int, Vector[Double])], labels: RDD[(Int, Boolean)]


    val test_files = s"${data_directory}lyrl2004_vectors_test_pt1.dat.gz"
    val test = spark.read.textFile(test_files)

    val train = spark.read.textFile(data_directory + "lyrl2004_vectors_train.dat.gz")
    val topics = spark.read.textFile(data_directory + "rcv1-v2.topics.qrels.gz")

    (test, train, topics)
  }

}