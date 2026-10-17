// port-lint: tests src/cauchy.rs
package io.github.kotlinmania.randdistr

import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class CauchyTest {
    private fun median(numbers: DoubleArray): Double {
        numbers.sort()
        val mid = numbers.size / 2
        return numbers[mid]
    }

    @Test
    fun testCauchyAverages() {
        val cauchy = Cauchy.new(10.0, 5.0).unwrap()
        val rng = testRng(123uL)
        val numbers = DoubleArray(1000)
        var sum = 0.0
        for (i in numbers.indices) {
            val v = cauchy.sample(rng)
            numbers[i] = v
            sum += v
        }
        val med = median(numbers)
        assertTrue(abs(med - 10.0) < 0.4)
        val mean = sum / 1000.0
        assertTrue(abs(mean - 10.0) > 0.4)
    }

    @Test
    fun testCauchyInvalidScaleZero() {
        assertFailsWith<IllegalStateException> {
            Cauchy.new(0.0, 0.0).unwrap()
        }
    }

    @Test
    fun testCauchyInvalidScaleNeg() {
        assertFailsWith<IllegalStateException> {
            Cauchy.new(0.0, -10.0).unwrap()
        }
    }

    @Test
    fun valueStability() {
        val distr64 = Cauchy.new(100.0, 10.0).unwrap()
        val rng64 = testRng(353uL)
        val buf64 = DoubleArray(4) { distr64.sample(rng64) }
        assertContentEquals(
            doubleArrayOf(
                77.93369152808678,
                90.1606912098641,
                125.31516221323625,
                86.10217834773925,
            ),
            buf64,
        )

        val distr32 = Cauchy.new(10.0, 7.0).unwrap()
        val rng32 = testRng(353uL)
        val buf32 = FloatArray(4) { distr32.sampleFloat(rng32) }
        val expected32 = floatArrayOf(15.023088f, -5.446413f, 3.7092876f, 3.112482f)
        for (i in 0 until 4) {
            assertAlmostEq(buf32[i].toDouble(), expected32[i].toDouble(), 1e-5)
        }
    }

    @Test
    fun cauchyDistributionsCanBeCompared() {
        assertEquals(Cauchy.new(1.0, 2.0), Cauchy.new(1.0, 2.0))
    }
}
