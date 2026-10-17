// port-lint: tests src/binomial.rs
package io.github.kotlinmania.randdistr

import io.github.kotlinmania.rand.Rng
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class BinomialTest {
    private fun testBinomialMeanAndVariance(
        n: ULong,
        p: Double,
        rng: Rng,
    ) {
        val binomial = Binomial.new(n, p).unwrap()

        val expectedMean = n.toDouble() * p
        val expectedVariance = n.toDouble() * p * (1.0 - p)

        val results = DoubleArray(1000)
        for (i in results.indices) {
            results[i] = binomial.sample(rng).toDouble()
        }

        val mean = results.sum() / results.size.toDouble()
        assertTrue(abs(mean - expectedMean) < expectedMean / 50.0)

        val variance = results.sumOf { (it - mean) * (it - mean) } / results.size.toDouble()
        assertTrue(abs(variance - expectedVariance) < expectedVariance / 10.0)
    }

    @Test
    fun testBinomial() {
        val rng = testRng(351uL)
        testBinomialMeanAndVariance(150uL, 0.1, rng)
        testBinomialMeanAndVariance(70uL, 0.6, rng)
        testBinomialMeanAndVariance(40uL, 0.5, rng)
        testBinomialMeanAndVariance(20uL, 0.7, rng)
        testBinomialMeanAndVariance(20uL, 0.5, rng)
        testBinomialMeanAndVariance(1uL shl 61, 1e-17, rng)
        testBinomialMeanAndVariance(ULong.MAX_VALUE, 1e-19, rng)
    }

    @Test
    fun testBinomialEndPoints() {
        val rng = testRng(352uL)
        assertEquals(0uL, rng.sample(Binomial.new(20uL, 0.0).unwrap()))
        assertEquals(20uL, rng.sample(Binomial.new(20uL, 1.0).unwrap()))
    }

    @Test
    fun testBinomialInvalidLambdaNeg() {
        assertFailsWith<IllegalStateException> {
            Binomial.new(20uL, -10.0).unwrap()
        }
    }

    @Test
    fun binomialDistributionsCanBeCompared() {
        assertEquals(Binomial.new(1uL, 1.0), Binomial.new(1uL, 1.0))
    }

    @Test
    fun binomialAvoidInfiniteLoop() {
        val dist = Binomial.new(16000000uL, 3.1444753148558566e-10).unwrap()
        var sum = 0uL
        val rng = testRng(742uL)
        repeat(100_000) {
            sum += dist.sample(rng)
        }
        assertNotEquals(0uL, sum)
    }
}
