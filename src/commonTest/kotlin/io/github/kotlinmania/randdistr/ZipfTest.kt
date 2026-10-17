// port-lint: tests src/zipf.rs
package io.github.kotlinmania.randdistr

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ZipfTest {
    @Test
    fun zipfSTooSmall() {
        assertFailsWith<IllegalStateException> {
            Zipf.new(10.0, -1.0).unwrap()
        }
    }

    @Test
    fun zipfNTooSmall() {
        assertFailsWith<IllegalStateException> {
            Zipf.new(0.0, 1.0).unwrap()
        }
    }

    @Test
    fun zipfNan() {
        assertFailsWith<IllegalStateException> {
            Zipf.new(10.0, Double.NaN).unwrap()
        }
    }

    @Test
    fun zipfSample() {
        val d = Zipf.new(10.0, 0.5).unwrap()
        val rng = testRng(2uL)
        repeat(1000) {
            val r = d.sample(rng)
            assertTrue(r >= 1.0)
        }
    }

    @Test
    fun zipfSampleS1() {
        val d = Zipf.new(10.0, 1.0).unwrap()
        val rng = testRng(2uL)
        repeat(1000) {
            val r = d.sample(rng)
            assertTrue(r >= 1.0)
        }
    }

    @Test
    fun zipfSampleS0() {
        val d = Zipf.new(10.0, 0.0).unwrap()
        val rng = testRng(2uL)
        repeat(1000) {
            val r = d.sample(rng)
            assertTrue(r >= 1.0)
        }
    }

    @Test
    fun zipfSampleLargeN() {
        val d = Zipf.new(Double.MAX_VALUE, 1.5).unwrap()
        val rng = testRng(2uL)
        repeat(1000) {
            val r = d.sample(rng)
            assertTrue(r >= 1.0)
        }
    }

    @Test
    fun zipfValueStability() {
        val distr32 = Zipf.new(10.0, 0.5).unwrap()
        val rng32 = testRng(213uL)
        val buf32 = FloatArray(4) { distr32.sampleFloat(rng32) }
        assertContentEquals(floatArrayOf(10.0f, 2.0f, 6.0f, 7.0f), buf32)

        val distr64 = Zipf.new(10.0, 2.0).unwrap()
        val rng64 = testRng(213uL)
        val buf64 = DoubleArray(4) { distr64.sample(rng64) }
        assertContentEquals(doubleArrayOf(1.0, 2.0, 3.0, 2.0), buf64)
    }

    @Test
    fun zipfDistributionsCanBeCompared() {
        assertEquals(Zipf.new(1.0, 2.0), Zipf.new(1.0, 2.0))
    }
}
