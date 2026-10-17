// port-lint: tests src/gumbel.rs
package io.github.kotlinmania.randdistr

import kotlin.math.abs
import kotlin.math.ln
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class GumbelTest {
    @Test
    fun testZeroScale() {
        assertFailsWith<IllegalStateException> {
            Gumbel.new(0.0, 0.0).unwrap()
        }
    }

    @Test
    fun testInfiniteScale() {
        assertFailsWith<IllegalStateException> {
            Gumbel.new(0.0, Double.POSITIVE_INFINITY).unwrap()
        }
    }

    @Test
    fun testNanScale() {
        assertFailsWith<IllegalStateException> {
            Gumbel.new(0.0, Double.NaN).unwrap()
        }
    }

    @Test
    fun testInfiniteLocation() {
        assertFailsWith<IllegalStateException> {
            Gumbel.new(Double.POSITIVE_INFINITY, 1.0).unwrap()
        }
    }

    @Test
    fun testNanLocation() {
        assertFailsWith<IllegalStateException> {
            Gumbel.new(Double.NaN, 1.0).unwrap()
        }
    }

    @Test
    fun testSampleAgainstCdf() {
        fun negLogLog(x: Double): Double = -ln(-ln(x))
        val location = 0.0
        val scale = 1.0
        val iterations = 100_000
        val increment = 1.0 / iterations.toDouble()
        val probabilities = doubleArrayOf(0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9)
        val quantiles = DoubleArray(9) { negLogLog(probabilities[it]) }
        val proportions = DoubleArray(9)
        val d = Gumbel.new(location, scale).unwrap()
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
    fun gumbelDistributionsCanBeCompared() {
        assertEquals(Gumbel.new(1.0, 2.0), Gumbel.new(1.0, 2.0))
    }
}
