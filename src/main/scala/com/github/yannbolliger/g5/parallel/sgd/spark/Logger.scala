package com.github.yannbolliger.g5.parallel.sgd.spark
import com.google.gson.Gson
import java.io._
import scala.collection.mutable.MutableList
import java.time.Instant

case class Log(time: String, loss_val: Double)
case class Logs(
    start_time: String,
    end_time: String,
    running_time: String,
    n_workers: Int,
    sync_epochs: Int,
    accuracy_train: Double,
    accuracy_1_train: Double,
    accuracy_min1_train: Double,
    accuracy_val: Double,
    accuracy_1_val: Double,
    accuracy_min1_val: Double,
    accuracy_test: Double,
    accuracy_1_test: Double,
    accuracy_min1_test: Double,
    losses_val: Double,
    losses_train: List[Log])

class Logger(n_workers: Int, sync_epochs: Int) {
  val start_time: Long = System.currentTimeMillis()
  val losses_train: MutableList[Log] = new MutableList()

  def appendLoss(loss_val: Double): Unit = {
    val newLog = new Log(format_date(System.currentTimeMillis()), loss_val)
    losses_train += newLog
  }

  def finish(
      accuracy_train: Double=0,
      accuracy_1_train: Double=0,
      accuracy_min1_train: Double=0,
      accuracy_val: Double=0,
      accuracy_1_val: Double=0,
      accuracy_min1_val: Double=0,
      accuracy_test: Double=0,
      accuracy_1_test: Double=0,
      accuracy_min1_test: Double=0,
      losses_val: Double=0): Unit = {
    val end_time = System.currentTimeMillis()
    val running_time = end_time - start_time

    val logs = new Logs(
      format_date(start_time),
      format_date(end_time),
      running_time.toString,
      n_workers,
      sync_epochs,
      accuracy_train,
      accuracy_1_train,
      accuracy_min1_train,
      accuracy_val,
      accuracy_1_val,
      accuracy_min1_val,
      accuracy_test,
      accuracy_1_test,
      accuracy_min1_test,
      losses_val,
      losses_train.toList
    )

    flush(logs)

  }

  def format_date(time_in_millis: Long): String = {
    Instant.ofEpochMilli(time_in_millis).formatted("%Y-%m-%d %H:%M:%S.%f")
  }

  def flush(logs: Logs): Unit = {
    val gson = new Gson
    val jsonString = gson.toJson(logs)
    val pw = new PrintWriter(new File("logs.txt"))
    pw.write(jsonString)
    pw.close
  }
}
