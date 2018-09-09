package com.hackathon.fellas.homedepotapp

import koma.extensions.set
import koma.matrix.Matrix
import koma.matrix.ejml.EJMLMatrixFactory

@Suppress("unused")
class MatrixUtils {
    companion object {
        val factory = EJMLMatrixFactory()

        fun unitVector(size: Int, basis: Int): Matrix<Double> {
            val result = factory.zeros(size, 1)
            result[basis] = 1.0
            return result
        }

        fun zeroVector(size: Int): Matrix<Double> {
            return factory.zeros(size, 1)
        }

        fun zeroMatrix(rows: Int, cols: Int): Matrix<Double> {
            return factory.zeros(rows, cols)
        }

        fun asMatrix(data: Array<Array<Double>>, rows: Int, cols: Int): Matrix<Double> {
            val result = zeroMatrix(rows, cols)
            for (i in 0 until rows) {
                for (j in 0 until cols) {
                    result[i, j] = data[i][j]
                }
            }
            return result
        }

        fun diag(data: Array<Double>): Matrix<Double> {
            val result = zeroMatrix(data.size, data.size)
            for (i in 0 until data.size) {
                result[i, i] = data[i]
            }
            return result
        }

        fun asMatrix(data: Array<Array<Double>>): Matrix<Double> {
            val rows = data.size
            val cols = data[0].size
            return asMatrix(data, rows, cols)
        }
    }
}
