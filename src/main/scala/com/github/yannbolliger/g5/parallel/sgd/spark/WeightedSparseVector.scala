package com.github.yannbolliger.g5.parallel.sgd.spark

case class WeightedSparseVector(
    sparseVector: SparseVector,
    private val countMap: Map[Int, Int]
) {

  def +(sparseVector: SparseVector): WeightedSparseVector =
    this + WeightedSparseVector(sparseVector)

  def +(other: WeightedSparseVector): WeightedSparseVector = {
    val newCountMap = (countMap.toSeq ++ other.countMap.toSeq)
      .groupBy(_._1)
      .mapValues(_.map(_._2).sum)
      // see above for map(identity)
      .map(identity)

    WeightedSparseVector(sparseVector + other.sparseVector, newCountMap)
  }

  def weightedAverageVector(): SparseVector =
    sparseVector / SparseVector(countMap.mapValues(_.toDouble))

}

object WeightedSparseVector {

  def empty: WeightedSparseVector =
    WeightedSparseVector(SparseVector.empty, Map.empty)

  def apply(sparseVector: SparseVector): WeightedSparseVector =
    new WeightedSparseVector(
      sparseVector,
      sparseVector.getNonZeroIndexes.map(key => key -> 1).toMap
    )
}
