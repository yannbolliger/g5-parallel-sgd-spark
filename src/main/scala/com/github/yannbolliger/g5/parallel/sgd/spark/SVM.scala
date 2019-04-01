package com.github.yannbolliger.g5.parallel.sgd.spark

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
      data: RDD[(Int, SparseVector, Boolean)],
      weights: Vector[Double]
  ): Vector[Double] = {

    val gradients = data
      .sample(withReplacement = false, fraction = batchFraction)
      .map { case (_, x, y) => gradient(x, weights, y) }
      .persist()

    val batchSize = gradients.count().toDouble

    val averageGradient = gradients.reduce(_ + _) / batchSize

    val newWeights = (averageGradient * -learningRate) + weights

    newWeights
  }

  def loss(
      data: RDD[(Int, SparseVector, Boolean)],
      weights: Vector[Double]
  ): Double = {

    val svmLoss = data.map {
      case (_, vector, label) => Math.max(0, 1 - ((vector * label) dot weights))
    }.sum

    val regularizerLoss: Double = Settings.lambda * weights
      .map(w => w * w)
      .sum

    1.0 / data.count * svmLoss + regularizerLoss
  }

  private implicit def bool2double(b: Boolean): Double = if (b) 1.0 else 0.0
}
