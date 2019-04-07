package com.github.yannbolliger.g5.parallel.sgd.spark
import org.scalactic.TolerantNumerics
import org.scalatest.FunSpec

class SparseVectorSpec extends FunSpec {

  describe("A SparseVector") {

    describe("when empty") {
      val emptySparseVector = SparseVector.empty
      val doubleVector = Vector(1.0, 2, 3)

      it("has size 0") {
        assert(emptySparseVector.size == 0)
      }

      it("doesn't have non-zero indexes") {
        assert(emptySparseVector.getNonZeroIndexes == Set())
      }

      it("stays empty on scalar addition") {
        assert(emptySparseVector + 1.3 == emptySparseVector)
      }

      it("stays empty on vector addition") {
        assert(emptySparseVector + doubleVector == doubleVector)
      }

      it("is zero on dot product") {
        assert((emptySparseVector dot doubleVector) == 0)
      }
    }

    describe("when sparsly filled") {
      val sparseVector = SparseVector(Map(2 -> 0.0, 4 -> 0.5, 20 -> 1.2))

      it("gives correct size") { assert(sparseVector.size == 3) }

      it("correctly adds a scalar") {
        assert(
          sparseVector + 3.2 == SparseVector(Map(2 -> 3.2, 4 -> 3.7, 20 -> 4.4))
        )
      }

      it("correctly subtracts a scalar") {
        assert(
          sparseVector - 3.2 === SparseVector(
            Map(2 -> -3.2, 4 -> -2.7, 20 -> -2)
          )
        )
      }

      it("correctly multiplies a scalar") {
        assert(
          sparseVector * 3.2 === SparseVector(
            Map(2 -> 0, 4 -> 1.6, 20 -> 3.84)
          )
        )
      }

      it("correctly adds another sparse vector") {
        val addedItself = sparseVector + sparseVector

        assert(addedItself.size === 3)
        assert(addedItself === sparseVector * 2)
      }

      it("correctly adds another sparse vector with different components") {
        val added = sparseVector + SparseVector(Map(0 -> 1, 10 -> 0.1))

        assert(added.size === 5)
        assert(
          added === SparseVector(
            Map(0 -> 1, 10 -> 0.1, 2 -> 0.0, 4 -> 0.5, 20 -> 1.2)
          ))
      }

      it("correctly adds a dense vector") {
        assert(
          (sparseVector + Vector(1.0, 2, 3, 4, 5)) === Vector(1, 2, 3, 4, 5.5)
        )
      }
    }

    describe("dot product") {
      val sparseVector = SparseVector(Map(0 -> 0.1, 2 -> 1.2, 4 -> 0.5))

      it("throws if dense vector is not long enough") {
        assertThrows[IndexOutOfBoundsException](sparseVector dot Vector(1, 2))
      }

      it("calculates dot product for equal size dense vector") {
        assert((sparseVector dot Vector(1, 1, 1, 1, 1)) === 1.8)
      }

      it("correctly calculates dot product") {
        assert((sparseVector dot Vector(2, 0, 0, -3, -1000)) === -499.8)
      }

      it("calculates dot product for longer dense vector") {
        assert((sparseVector dot Vector(1, 1, 1, 1, 1, 1)) === 1.8)
      }
    }

    describe("parse from String") {
      it("adds bias term in front of vector") {
        val line = " 1111 1:0.001 2:0.01  200:20.3 "

        assert(
          SparseVector.fromStringWithBias(line) ===
            (1111, SparseVector(Map(0 -> 1, 1 -> 0.001, 2 -> 0.01, 200 -> 20.3)))
        )
      }
    }
  }
}
