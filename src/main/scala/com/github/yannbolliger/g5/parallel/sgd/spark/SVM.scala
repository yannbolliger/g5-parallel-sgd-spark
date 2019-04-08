package com.github.yannbolliger.g5.parallel.sgd.spark

import scala.language.implicitConversions
import com.github.yannbolliger.g5.parallel.sgd.spark.DataHelper.LabelledData
import org.apache.spark.rdd.RDD

class SVM(
    learningRate: Double,
    lambda: Double,
    batchFraction: Double,
    dimension: Int
) extends Serializable {

  private implicit def bool2double(b: Boolean): Double = if (b) 1.0 else -1.0

  def predict(x: SparseVector, weights: Vector[Double]): Boolean =
    (x dot weights) > 0

  def initialWeights: Vector[Double] =
    // also have a bias term: + 1
    Vector.fill(dimension + 1)(0)

  def regularizerGradient(x: SparseVector, weights: Vector[Double]): Double =
    2 * lambda * x.getNonZeroIndexes.map(key => weights(key)).sum / x.size

  def gradient(
      vector: SparseVector,
      weights: Vector[Double],
      label: Boolean
  ): SparseVector = {

    val regularizer: Double = regularizerGradient(vector, weights)

    if ((vector dot weights) * label < 1)
      vector * label - regularizer
    else
      // replace vector's components with -regularizer
      vector - (vector + regularizer)
  }

  def fitEpoch(
      data: RDD[LabelledData],
      weights: Vector[Double]
  ): Vector[Double] = {

    val gradients = data
      .sample(withReplacement = false, fraction = batchFraction)
      .mapValues { case (x, y) => gradient(x, weights, y) }
      .persist

    val averageGradient =
      gradients
        .aggregate(WeightedSparseVector.empty)(_ + _._2, _ + _)
        .weightedAverageVector()

    (averageGradient * learningRate) + weights
  }

  def svmLoss(data: RDD[LabelledData], weights: Vector[Double]): Double =
    data
      .mapValues {
        case (vector, label) => Math.max(0, 1 - label * (vector dot weights))
      }
      .aggregate(0.0)(_ + _._2, _ + _)

  def regularizerLoss(
      data: RDD[LabelledData],
      weights: Vector[Double]
  ): Double =
    data
      .mapValues {
        case (vector, _) =>
          lambda * vector.getNonZeroIndexes
            .map(key => Math.pow(weights(key), 2))
            .sum / vector.size
      }
      .aggregate(0.0)(_ + _._2, _ + _)

  def loss(data: RDD[LabelledData], weights: Vector[Double]): Double =
    (1.0 / data.count) * (
      svmLoss(data, weights) + regularizerLoss(data, weights)
    )

}
