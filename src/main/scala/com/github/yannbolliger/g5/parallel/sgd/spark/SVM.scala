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

  def isMisclassified(
      vector: SparseVector,
      weights: Vector[Double],
      label: Boolean
  ): Boolean = (vector dot weights) * label < 1

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

    if (isMisclassified(vector, weights, label))
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

    //val batchSize = gradients.count.toDouble

    val averageGradient =
      gradients.aggregate(SparseVector.empty)(_ + _._2, _ + _)

    val newWeights = (averageGradient * learningRate) + weights

    newWeights
  }

  def predict(x: SparseVector, weights: Vector[Double]): Boolean =
    (x dot weights) > 0

  def svmLoss(data: RDD[LabelledData], weights: Vector[Double]): Double =
    (1.0 / data.count) * data
      .mapValues {
        case (vector, label) => Math.max(0, 1 - ((vector dot weights) * label))
      }
      .aggregate(0.0)(_ + _._2, _ + _)

  def regularizerLoss(weights: Vector[Double]): Double =
    Settings.lambda * weights.map(w => w * w).sum

  def loss(data: RDD[LabelledData], weights: Vector[Double]): Double =
    svmLoss(data, weights) + regularizerLoss(weights)

}
