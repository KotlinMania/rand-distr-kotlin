// port-lint: tests src/frechet.rs
package io.github.kotlinmania.randdistr

import kotlin.math.abs
import kotlin.math.ln
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class FrechetTest {
    @Test
    fun testZeroScale() {
        assertFailsWith<IllegalStateException> {
            Frechet.new(0.0, 0.0, 1.0).unwrap()
        }
    }

    @Test
    fun testInfiniteScale() {
        assertFailsWith<IllegalStateException> {
            Frechet.new(0.0, Double.POSITIVE_INFINITY, 1.0).unwrap()
        }
    }

    @Test
    fun testNanScale() {
        assertFailsWith<IllegalStateException> {
            Frechet.new(0.0, Double.NaN, 1.0).unwrap()
        }
    }

    @Test
    fun testZeroShape() {
        assertFailsWith<IllegalStateException> {
            Frechet.new(0.0, 1.0, 0.0).unwrap()
        }
    }

    @Test
    fun testInfiniteShape() {
        assertFailsWith<IllegalStateException> {
            Frechet.new(0.0, 1.0, Double.POSITIVE_INFINITY).unwrap()
        }
    }

    @Test
    fun testNanShape() {
        assertFailsWith<IllegalStateException> {
            Frechet.new(0.0, 1.0, Double.NaN).unwrap()
        }
    }

    @Test
    fun testInfiniteLocation() {
        assertFailsWith<IllegalStateException> {
            Frechet.new(Double.POSITIVE_INFINITY, 1.0, 1.0).unwrap()
        }
    }

    @Test
    fun testNanLocation() {
        assertFailsWith<IllegalStateException> {
            Frechet.new(Double.NaN, 1.0, 1.0).unwrap()
        }
    }

    @Test
    fun testSampleAgainstCdf() {
        fun quantileFunction(x: Double): Double = 1.0 / (-ln(x))
        val location = 0.0
        val scale = 1.0
        val shape = 1.0
        val iterations = 100_000
        val increment = 1.0 / iterations.toDouble()
        val probabilities = doubleArrayOf(0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9)
        val quantiles = DoubleArray(9) { quantileFunction(probabilities[it]) }
        val proportions = DoubleArray(9)
        val d = Frechet.new(location, scale, shape).unwrap()
        val rng = testRng(1uL)
        repeat(iterations) {
            val replicate = d.sample(rng)
            for (i in quantiles.indices) {
                if (replicate < quantiles[i]) {
                    proportions[i] += increment
                }
            }
        }
        assertTrue(
            proportions.indices.all { i -> abs(proportions[i] - probabilities[i]) < 0.003 },
        )
    }

    @Test
    fun frechetDistributionsCanBeCompared() {
        assertEquals(Frechet.new(1.0, 2.0, 3.0), Frechet.new(1.0, 2.0, 3.0))
    }
}
