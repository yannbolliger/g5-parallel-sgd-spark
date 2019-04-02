package com.github.yannbolliger.g5.parallel.sgd.spark

import com.github.yannbolliger.g5.parallel.sgd.spark.DataHelper.LabelledData
import org.apache.spark.rdd.RDD

class SVM(
    learningRate: Double,
    lambda: Double,
    batchFraction: Double,
    dimension: Int
) extends Serializable {

  def initialWeights: Vector[Double] = Vector.fill(dimension)(0)

  def regularizerGradient(x: SparseVector, weights: Vector[Double]): Double =
    2 * lambda * x.getNonZeroIndexes.map(key => weights(key)).sum / x.size

  def gradient(
      vector: SparseVector,
      weights: Vector[Double],
      label: Boolean
  ): SparseVector = {

    val regularizer: Double = regularizerGradient(vector, weights)

    vector * label - regularizer
  }

  def fitEpoch(
      data: RDD[LabelledData],
      weights: Vector[Double]
  ): Vector[Double] = {

    val gradients = data
      .sample(withReplacement = false, fraction = batchFraction)
      .mapValues { case (x, y) => gradient(x, weights, y) }
      .persist

    val batchSize = gradients.count.toDouble

    val averageGradient =
      gradients.aggregate(SparseVector(Map.empty))(_ + _._2, _ + _) / batchSize

    val newWeights = (averageGradient * -learningRate) + weights

    newWeights
  }

  def loss(data: RDD[LabelledData], weights: Vector[Double]): Double = {

    val svmLoss = data
      .mapValues {
        case (vector, label) => Math.max(0, 1 - ((vector * label) dot weights))
      }
      .aggregate(0.0)(_ + _._2, _ + _)

    val regularizerLoss: Double = Settings.lambda * weights
      .map(w => w * w)
      .sum

    1.0 / data.count * svmLoss + regularizerLoss
  }

  private implicit def bool2double(b: Boolean): Double = if (b) 1.0 else 0.0
}
