// port-lint: tests src/skew_normal.rs
package io.github.kotlinmania.randdistr

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class SkewNormalTest {
    @Test
    fun invalidScaleNan() {
        assertFailsWith<IllegalStateException> {
            SkewNormal.new(0.0, Double.NaN, 0.0).unwrap()
        }
    }

    @Test
    fun invalidScaleZero() {
        assertFailsWith<IllegalStateException> {
            SkewNormal.new(0.0, 0.0, 0.0).unwrap()
        }
    }

    @Test
    fun invalidScaleNegative() {
        assertFailsWith<IllegalStateException> {
            SkewNormal.new(0.0, -1.0, 0.0).unwrap()
        }
    }

    @Test
    fun invalidScaleInfinite() {
        assertFailsWith<IllegalStateException> {
            SkewNormal.new(0.0, Double.POSITIVE_INFINITY, 0.0).unwrap()
        }
    }

    @Test
    fun invalidShapeNan() {
        assertFailsWith<IllegalStateException> {
            SkewNormal.new(0.0, 1.0, Double.NaN).unwrap()
        }
    }

    @Test
    fun invalidShapeInfinite() {
        assertFailsWith<IllegalStateException> {
            SkewNormal.new(0.0, 1.0, Double.POSITIVE_INFINITY).unwrap()
        }
    }

    @Test
    fun validLocationNan() {
        SkewNormal.new(Double.NaN, 1.0, 0.0).unwrap()
    }

    @Test
    fun skewNormalValueStability() {
        val distr32 = SkewNormal.new(0.0, 1.0, 0.0).unwrap()
        val rng32 = testRng(213uL)
        val buf32 = FloatArray(4) { distr32.sampleFloat(rng32) }
        assertContentEquals(
            floatArrayOf(-0.11844189f, 0.781378f, 0.06563994f, -1.1932899f),
            buf32,
        )

        val distr64 = SkewNormal.new(0.0, 1.0, 0.0).unwrap()
        val rng64 = testRng(213uL)
        val buf64 = DoubleArray(4) { distr64.sample(rng64) }
        assertContentEquals(
            doubleArrayOf(
                -0.11844188827977231,
                0.7813779637772346,
                0.06563993969580051,
                -1.1932899004186373,
            ),
            buf64,
        )

        val distrInf = SkewNormal.new(Double.POSITIVE_INFINITY, 1.0, 0.0).unwrap()
        val rngInf = testRng(213uL)
        val bufInf = DoubleArray(4) { distrInf.sample(rngInf) }
        assertContentEquals(
            doubleArrayOf(
                Double.POSITIVE_INFINITY,
                Double.POSITIVE_INFINITY,
                Double.POSITIVE_INFINITY,
                Double.POSITIVE_INFINITY,
            ),
            bufInf,
        )

        val distrNegInf = SkewNormal.new(Double.NEGATIVE_INFINITY, 1.0, 0.0).unwrap()
        val rngNegInf = testRng(213uL)
        val bufNegInf = DoubleArray(4) { distrNegInf.sample(rngNegInf) }
        assertContentEquals(
            doubleArrayOf(
                Double.NEGATIVE_INFINITY,
                Double.NEGATIVE_INFINITY,
                Double.NEGATIVE_INFINITY,
                Double.NEGATIVE_INFINITY,
            ),
            bufNegInf,
        )
    }

    @Test
    fun skewNormalValueLocationNan() {
        val skewNormal = SkewNormal.new(Double.NaN, 1.0, 0.0).unwrap()
        val rng = testRng(213uL)
        val buf = DoubleArray(4) { skewNormal.sample(rng) }
        for (value in buf) {
            assertTrue(value.isNaN())
        }
    }

    @Test
    fun skewNormalDistributionsCanBeCompared() {
        assertEquals(
            SkewNormal.new(1.0, 2.0, 3.0),
            SkewNormal.new(1.0, 2.0, 3.0),
        )
    }
}
