// port-lint: tests src/dirichlet.rs
package io.github.kotlinmania.randdistr

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class DirichletTest {
    @Test
    fun testDirichlet() {
        val d = Dirichlet.new(doubleArrayOf(1.0, 2.0, 3.0)).unwrap()
        val rng = testRng(221uL)
        val samples = d.sample(rng)
        assertTrue(samples.all { it > 0.0 })
    }

    @Test
    fun testDirichletInvalidLength() {
        assertFailsWith<IllegalStateException> {
            Dirichlet.new(doubleArrayOf(0.5)).unwrap()
        }
    }

    @Test
    fun testDirichletAlphaZero() {
        assertFailsWith<IllegalStateException> {
            Dirichlet.new(doubleArrayOf(0.1, 0.0, 0.3)).unwrap()
        }
    }

    @Test
    fun testDirichletAlphaNegative() {
        assertFailsWith<IllegalStateException> {
            Dirichlet.new(doubleArrayOf(0.1, -1.5, 0.3)).unwrap()
        }
    }

    @Test
    fun testDirichletAlphaNan() {
        assertFailsWith<IllegalStateException> {
            Dirichlet.new(doubleArrayOf(0.5, Double.NaN, 0.25)).unwrap()
        }
    }

    @Test
    fun testDirichletAlphaSubnormal() {
        assertFailsWith<IllegalStateException> {
            Dirichlet.new(doubleArrayOf(0.5, 1.5e-321, 0.25)).unwrap()
        }
    }

    @Test
    fun testDirichletAlphaInf() {
        assertFailsWith<IllegalStateException> {
            Dirichlet.new(doubleArrayOf(0.5, Double.POSITIVE_INFINITY, 0.25)).unwrap()
        }
    }

    @Test
    fun dirichletDistributionsCanBeCompared() {
        assertEquals(
            Dirichlet.new(doubleArrayOf(1.0, 2.0)),
            Dirichlet.new(doubleArrayOf(1.0, 2.0)),
        )
    }

    private fun checkDirichletMeans(
        alpha: DoubleArray,
        n: Int,
        rtol: Double,
        seed: ULong,
    ) {
        val d = Dirichlet.new(alpha).unwrap()
        val rng = testRng(seed)
        val sums = DoubleArray(alpha.size)
        repeat(n) {
            val samples = d.sample(rng)
            for (i in alpha.indices) {
                sums[i] += samples[i]
            }
        }
        val sampleMean = DoubleArray(alpha.size) { sums[it] / n.toDouble() }
        val alphaSum = alpha.sum()
        val expectedMean = DoubleArray(alpha.size) { alpha[it] / alphaSum }
        for (i in alpha.indices) {
            assertAlmostEq(sampleMean[i], expectedMean[i], rtol)
        }
    }

    @Test
    fun testDirichletMeans() {
        val n = 20000
        val rtol = 2e-2
        val seed = 1317624576693539401uL
        checkDirichletMeans(doubleArrayOf(0.5, 0.25), n, rtol, seed)
        checkDirichletMeans(doubleArrayOf(123.0, 75.0), n, rtol, seed)
        checkDirichletMeans(doubleArrayOf(2.0, 2.5, 5.0, 7.0), n, rtol, seed)
        checkDirichletMeans(doubleArrayOf(0.1, 8.0, 1.0, 2.0, 2.0, 0.85, 0.05, 12.5), n, rtol, seed)
    }

    @Test
    fun testDirichletMeansVerySmallAlpha() {
        val alpha = doubleArrayOf(0.001, 0.001, 0.001)
        val n = 10000
        val rtol = 1e-2
        val seed = 1317624576693539401uL
        checkDirichletMeans(alpha, n, rtol, seed)
    }

    @Test
    fun testDirichletMeansSmallAlpha() {
        val alpha = doubleArrayOf(0.05, 0.025, 0.075, 0.05)
        val n = 150000
        val rtol = 1e-3
        val seed = 1317624576693539401uL
        checkDirichletMeans(alpha, n, rtol, seed)
    }
}
