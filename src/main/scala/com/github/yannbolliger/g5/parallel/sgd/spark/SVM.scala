package com.github.yannbolliger.g5.parallel.sgd.spark

import org.apache.spark.rdd.RDD

class SVM(
    learningRate: Double,
    lambda: Double,
    batchFraction: Double,
    dimension: Int
) {
  implicit def bool2double(b:Boolean) = if (b) 1.0 else 0.0
  implicit def bool2double(b:Boolean) = if (b) 1.0 else 0.0
  def initialWeights: Vector[Double] = Vector.fill(dimension)(0)

  def regularizerGradient(x: SparseVector, weights: Vector[Double]):Double = {
    2 * lambda * x.getKeys.map(key => weights.apply(key)).sum / x.size
  }

  def gradient(x: SparseVector, weights: Vector[Double], label: Boolean): SparseVector = {


    val regularizer: Double = regularizerGradient(x, weights)

    x * label - regularizer
  }

  def fitEpoch(
      data: RDD[(Int, SparseVector, Boolean)],
      weights: Vector[Double]
  ): Vector[Double] = {

    val gradients = data
      .sample(withReplacement = false, fraction = batchFraction)
      .map(trainData => gradient(trainData._2, weights, trainData._3))
      .persist()

    val batchSize = gradients.count().toDouble

    val averageGradient = gradients.reduce(_+_) /  batchSize

    val newWeights = (averageGradient * -learningRate) + weights

    newWeights
  }
}
