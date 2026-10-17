// port-lint: tests src/hypergeometric.rs
package io.github.kotlinmania.randdistr

import io.github.kotlinmania.rand.Rng
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HypergeometricTest {
    @Test
    fun testHypergeometricInvalidParams() {
        assertTrue(Hypergeometric.new(100uL, 101uL, 5uL).isErr())
        assertTrue(Hypergeometric.new(100uL, 10uL, 101uL).isErr())
        assertTrue(Hypergeometric.new(100uL, 101uL, 101uL).isErr())
        assertTrue(Hypergeometric.new(100uL, 10uL, 5uL).isOk())
    }

    private fun testHypergeometricMeanAndVariance(
        n: ULong,
        k: ULong,
        s: ULong,
        rng: Rng,
    ) {
        val distr = Hypergeometric.new(n, k, s).unwrap()

        val expectedMean = s.toDouble() * k.toDouble() / n.toDouble()
        val expectedVariance =
            run {
                val numerator = (s * k * (n - k) * (n - s)).toDouble()
                val denominator = (n * n * (n - 1uL)).toDouble()
                numerator / denominator
            }

        val results = DoubleArray(1000)
        for (i in results.indices) {
            results[i] = distr.sample(rng).toDouble()
        }

        val mean = results.sum() / results.size.toDouble()
        assertTrue(abs(mean - expectedMean) < expectedMean / 50.0)

        val variance = results.sumOf { (it - mean) * (it - mean) } / results.size.toDouble()
        assertTrue(abs(variance - expectedVariance) < expectedVariance / 10.0)
    }

    @Test
    fun testHypergeometric() {
        val rng = testRng(737uL)

        testHypergeometricMeanAndVariance(500uL, 400uL, 30uL, rng)
        testHypergeometricMeanAndVariance(250uL, 200uL, 230uL, rng)
        testHypergeometricMeanAndVariance(100uL, 20uL, 6uL, rng)
        testHypergeometricMeanAndVariance(50uL, 10uL, 47uL, rng)

        testHypergeometricMeanAndVariance(5000uL, 2500uL, 500uL, rng)
        testHypergeometricMeanAndVariance(10100uL, 10000uL, 1000uL, rng)
        testHypergeometricMeanAndVariance(100100uL, 100uL, 10000uL, rng)
    }

    @Test
    fun hypergeometricDistributionsCanBeCompared() {
        assertEquals(Hypergeometric.new(1uL, 2uL, 3uL), Hypergeometric.new(1uL, 2uL, 3uL))
    }

    @Test
    fun stirling() {
        val test = doubleArrayOf(0.5, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0)
        val expectedLnGamma =
            doubleArrayOf(
                ln(0.5 * sqrt(PI)),
                0.0,
                ln(2.0),
                ln(6.0),
                ln(24.0),
                ln(120.0),
                ln(720.0),
                ln(5040.0),
                ln(40320.0),
            )
        for (i in test.indices) {
            val lnFac = lnOfFactorial(test[i])
            assertTrue(abs(expectedLnGamma[i] - lnFac) < 1e-4)
        }
    }
}
