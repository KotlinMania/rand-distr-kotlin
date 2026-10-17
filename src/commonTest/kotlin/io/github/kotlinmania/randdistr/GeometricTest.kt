// port-lint: tests src/geometric.rs
package io.github.kotlinmania.randdistr

import io.github.kotlinmania.rand.Rng
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GeometricTest {
    @Test
    fun testGeoInvalidP() {
        assertTrue(Geometric.new(Double.NaN).isErr())
        assertTrue(Geometric.new(Double.POSITIVE_INFINITY).isErr())
        assertTrue(Geometric.new(Double.NEGATIVE_INFINITY).isErr())

        assertTrue(Geometric.new(-0.5).isErr())
        assertTrue(Geometric.new(0.0).isOk())
        assertTrue(Geometric.new(1.0).isOk())
        assertTrue(Geometric.new(2.0).isErr())
    }

    private fun testGeoMeanAndVariance(
        p: Double,
        rng: Rng,
    ) {
        val distr = Geometric.new(p).unwrap()

        val expectedMean = (1.0 - p) / p
        val expectedVariance = (1.0 - p) / (p * p)

        val results = DoubleArray(10000)
        for (i in results.indices) {
            results[i] = distr.sample(rng).toDouble()
        }

        val mean = results.sum() / results.size.toDouble()
        assertTrue(abs(mean - expectedMean) < expectedMean / 40.0)

        val variance = results.sumOf { (it - mean) * (it - mean) } / results.size.toDouble()
        assertTrue(abs(variance - expectedVariance) < expectedVariance / 10.0)
    }

    @Test
    fun testGeometric() {
        val rng = testRng(12345uL)

        testGeoMeanAndVariance(0.10, rng)
        testGeoMeanAndVariance(0.25, rng)
        testGeoMeanAndVariance(0.50, rng)
        testGeoMeanAndVariance(0.75, rng)
        testGeoMeanAndVariance(0.90, rng)
    }

    @Test
    fun testStandardGeometric() {
        val rng = testRng(654321uL)

        val distr = StandardGeometric
        val expectedMean = 1.0
        val expectedVariance = 2.0

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
    fun geometricDistributionsCanBeCompared() {
        assertEquals(Geometric.new(1.0), Geometric.new(1.0))
    }
}
