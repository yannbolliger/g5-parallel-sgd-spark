package com.github.yannbolliger.g5.parallel.sgd.spark
import org.apache.spark.SparkContext
import org.scalatest.FunSpec

class SVMSpec extends FunSpec {

  val sc = new SparkContext("local[4]", "test")

  describe("svm loss") {
    val svm = new SVM(0.5, 0.5, 0.5, 2)

    val data = List(
      (1, (SparseVector(Map(0 -> 1, 1 -> 100, 2 -> 2)), true)),
      (2, (SparseVector(Map(0 -> 1, 1 -> -100, 2 -> 3)), true)),
      (3, (SparseVector(Map(0 -> 1, 1 -> 10, 2 -> 0)), false)),
      (4, (SparseVector(Map(0 -> 1, 1 -> -1000, 2 -> -1)), false))
    )

    it("gives loss 0 if examples are seperated") {
      val weights = Vector(-1.0, 0, 1)
      assert(svm.svmLoss(sc.parallelize(data), weights) === 0)
    }

    it("gives loss 1 if everything false") {
      val weights = Vector(0, 0, 0.0)
      assert(svm.svmLoss(sc.parallelize(data), weights) === 1)
    }
  }
}
