package com.github.yannbolliger.g5.parallel.sgd.spark

class SparseVector(vectorMap: Map[Int, Double]) {

  def size: Int = {
    vectorMap.size
  }

  def getKeys: Iterable[Int] = {
    vectorMap.keys
  }

  def +(scalar: Double): SparseVector =
    SparseVector(vectorMap.mapValues(_ + scalar))

  def -(scalar: Double): SparseVector = this + (-1 * scalar)

  def *(scalar: Double): SparseVector =
  SparseVector(vectorMap.mapValues(_ * scalar))

  def /(scalar: Double): SparseVector = this * (1 / scalar)


  def +(other: SparseVector): SparseVector = {
    val newMap = (vectorMap.toSeq ++ other.vectorMap.toSeq)
      .groupBy(_._1)
      .mapValues(_.map(_._2).sum)

    SparseVector(newMap)
  }

  def -(other: SparseVector): SparseVector = this + (other * -1)

  def *(vector: Vector[Double]): SparseVector = {
    val newMap = vectorMap.map {
      case (key, value) =>
        (key, vector(key) * value)
    }

    SparseVector(newMap)
  }

  def +(vector: Vector[Double]): Vector[Double] = {
    val newVector = vector.zipWithIndex.map {
      case (value, idx) => value + vectorMap.getOrElse(idx, 0)
    }
    newVector
  }

}

object SparseVector {

  def apply(vectorMap: Map[Int, Double]): SparseVector =
    new SparseVector(vectorMap)

  def fromString(line: String): (Int, SparseVector) = {
    val idString :: data: List[String] = line.trim.split(raw"\s+")

    val id: Int = idString.toInt

    val vectorMap: Map[Int, Double] = data
      .map(keyValuePair => {
        val (key: String) :: (value: String) :: _ = keyValuePair.split(":", 1)
        key.toInt -> value.toDouble
      })
      .toMap

    (id, SparseVector(vectorMap))
  }
}
