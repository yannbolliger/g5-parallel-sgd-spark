package com.github.yannbolliger.g5.parallel.sgd.spark

import com.github.yannbolliger.g5.parallel.sgd.spark.DataHelper.LabelledData
import org.apache.spark.rdd.RDD

class SVM(
    learningRate: Double,
    lambda: Double,
    batchFraction: Double,
    dimension: Int
) extends Serializable {

  def isMissclassified(
      vector: SparseVector,
      weights: Vector[Double],
      label: Boolean
  ): Boolean = (vector dot weights) * label < 1

  def initialWeights: Vector[Double] = Vector.fill(dimension)(0)

  def regularizerGradient(x: SparseVector, weights: Vector[Double]): Double =
    2 * lambda * x.getNonZeroIndexes.map(key => weights(key)).sum / x.size

  def gradient(
      vector: SparseVector,
      weights: Vector[Double],
      label: Boolean
  ): SparseVector = {

    val regularizer: Double = regularizerGradient(vector, weights)

    if (isMissclassified(vector, weights, label))
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
      gradients.aggregate(SparseVector(Map.empty))(_ aggregate _._2, _ aggregate _).getAverageGradient()

    val newWeights = (averageGradient * learningRate) + weights

    newWeights
  }

  def loss(data: RDD[LabelledData], weights: Vector[Double]): Double = {

    val svmLoss = data
      .mapValues {
        case (vector, label) => Math.max(0, 1 - label * (vector dot weights))
      }
      .aggregate(0.0)(_ + _._2, _ + _)

    val regularizerLoss: Double = Settings.lambda * weights
      .map(w => w * w)
      .sum

    1.0 / data.count * svmLoss + regularizerLoss
  }

  def predict(x: SparseVector, weights: Vector[Double]): Boolean =
    (x dot weights) > 0

  private implicit def bool2double(b: Boolean): Double = if (b) 1.0 else -1.0
}
