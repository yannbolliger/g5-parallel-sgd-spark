package com.github.yannbolliger.g5.parallel.sgd.spark

case class SparseVector(private val vectorMap: Map[Int, Double]) {

  def size: Int = vectorMap.size

  def getNonZeroIndexes: Iterable[Int] = vectorMap.keys

  def +(scalar: Double): SparseVector =
    // mapValues has a bug and cannot be serialized in Scala 2.11
    // see https://github.com/scala/bug/issues/7005
    // therefore we need to add something at the end
    SparseVector(vectorMap.mapValues(_ + scalar).map(identity))

  def -(scalar: Double): SparseVector = this + (-1 * scalar)

  def *(scalar: Double): SparseVector =
    // see above for map(identity)
    SparseVector(vectorMap.mapValues(_ * scalar).map(identity))

  def /(scalar: Double): SparseVector = this * (1 / scalar)

  def +(other: SparseVector): SparseVector = {
    val newMap = (vectorMap.toSeq ++ other.vectorMap.toSeq)
      .groupBy(_._1)
      .mapValues(_.map(_._2).sum)
      // see above for map(identity)
      .map(identity)

    SparseVector(newMap)
  }

  def -(other: SparseVector): SparseVector = this + (other * -1)

  def dot(vector: Vector[Double]): Double = {
    val newMap = vectorMap.map {
      case (key, value) => (key, vector(key) * value)
    }

    newMap.values.sum
  }

  def +(vector: Vector[Double]): Vector[Double] = {
    val newVector = vector.zipWithIndex.map {
      case (value, idx) => value + vectorMap.getOrElse(idx, 0.0)
    }

    newVector
  }

}

object SparseVector {

  def empty: SparseVector = SparseVector(Map.empty)

  def fromStringWithBias(line: String): (Int, SparseVector) = {
    val idString :: data = line.trim.split(raw"\s+").toList

    val id: Int = idString.toInt

    val vectorMap: Map[Int, Double] = data
      .map(keyValuePair => {
        val key :: value :: _ = keyValuePair.split(":").toList

        (key.toInt) -> value.toDouble
      })
      .toMap

    (id, SparseVector(vectorMap + (0 -> 1.0)))
  }
}
