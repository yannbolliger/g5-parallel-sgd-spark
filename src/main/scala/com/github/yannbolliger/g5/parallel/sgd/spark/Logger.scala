package com.github.yannbolliger.g5.parallel.sgd.spark
import com.google.gson.Gson
import java.io._

case class Log(time: String, loss_val: Double)
case class Logs(start_time: String, end_time: String, running_time: String,
                n_workers: Int, sync_epochs: Int, accuracy_train: Double,
                accuracy_1_train: acc_pos_train, accuracy_min1_train: Double, accuracy_val: Double,
                accuracy_1_val: Double, accuracy_min1_val: Double, accuracy_test: Double,
                accuracy_1_test: Double, accuracy_min1_test: Double, losses_val: Double,
                losses_train: List[Log])

object Logger {
  def writeLogs(logs: Logs) = {
    val gson = new Gson
    val jsonString = gson.toJson(logs)
    val pw = new PrintWriter(new File("logs.txt" ))
    pw.write(jsonString)
    pw.close
  }

}
