package com.github.yannbolliger.g5.parallel.sgd.spark

import org.apache.spark.rdd.RDD

class SVM(
    learningRate: Double,
    lambda: Double,
    batchFraction: Double,
    dimension: Int
) {

  def initialWeights: Vector[Double] = Vector.fill(dimension)(0)

  def fit(
      data: RDD[SparseVector],
      weights: Vector[Double]
  ): Vector[Double] = {

    data
      .sample(withReplacement = false, fraction = batchFraction)
      .map(x => gradient(x, weights))
  }
}
