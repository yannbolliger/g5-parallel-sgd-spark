package com.github.yannbolliger.g5.parallel.sgd.spark

import com.google.gson.Gson
import java.io._

import scala.collection.JavaConverters._
import scala.collection.mutable
import java.time.Instant
import java.util.Date

class Logger(settings: Settings) {
  val start_time: Long = System.currentTimeMillis()
  val lossesVal: mutable.MutableList[Log] = mutable.MutableList()
  val running_mode: String = "Spark"

  case class Log(time: String, loss_val: Double)
  case class Logs(
      start_time: String,
      end_time: String,
      running_time: String,
      running_mode: String,
      n_workers: Int,
      sync_epochs: Int,
      accuracy_train: Double,
      accuracy_val: Double,
      accuracy_test: Double,
      losses_val: java.util.List[Log]
  )

  def appendLoss(loss_val: Double): Unit = {
    val newLog = Log(format_date(System.currentTimeMillis()), loss_val)
    lossesVal += newLog
  }

  def finish(
      accuracy_train: Double,
      accuracy_val: Double,
      accuracy_test: Double
  ): Unit = {
    val end_time = System.currentTimeMillis()
    val running_time = end_time - start_time

    val logs = Logs(
      format_date(start_time),
      format_date(end_time),
      running_time.toString,
      running_mode,
      settings.numberWorkers,
      settings.epochs,
      accuracy_train,
      accuracy_val,
      accuracy_test,
      lossesVal.asJava
    )

    flush(logs)

  }

  def format_date(time_in_millis: Long): String = {
    Instant.ofEpochMilli(time_in_millis).toString
  }

  def flush(logs: Logs): Unit = {
    val jsonString = (new Gson).toJson(logs)

    val filename = settings.dataPath +
      s"/logs/logs_${new Date()}_n${settings.numberWorkers}_s${settings.subsetPerWorker}.json"

    val pw = new PrintWriter(new File(filename))
    pw.write(jsonString)
    pw.close()
  }
}
