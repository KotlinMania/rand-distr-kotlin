// port-lint: tests src/zeta.rs
package io.github.kotlinmania.randdistr

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ZetaTest {
    @Test
    fun zetaInvalid() {
        assertFailsWith<IllegalStateException> {
            Zeta.new(1.0).unwrap()
        }
    }

    @Test
    fun zetaNan() {
        assertFailsWith<IllegalStateException> {
            Zeta.new(Double.NaN).unwrap()
        }
    }

    @Test
    fun zetaSample() {
        val a = 2.0
        val d = Zeta.new(a).unwrap()
        val rng = testRng(1uL)
        repeat(1000) {
            val r = d.sample(rng)
            assertTrue(r >= 1.0)
        }
    }

    @Test
    fun zetaSmallA() {
        val a = 1.0 + 1e-15
        val d = Zeta.new(a).unwrap()
        val rng = testRng(2uL)
        repeat(1000) {
            val r = d.sample(rng)
            assertTrue(r >= 1.0)
        }
    }

    @Test
    fun zetaValueStability() {
        val distr32 = Zeta.new(1.5).unwrap()
        val rng32 = testRng(213uL)
        val buf32 = FloatArray(4) { distr32.sampleFloat(rng32) }
        assertContentEquals(floatArrayOf(1.0f, 2.0f, 1.0f, 1.0f), buf32)

        val distr64 = Zeta.new(2.0).unwrap()
        val rng64 = testRng(213uL)
        val buf64 = DoubleArray(4) { distr64.sample(rng64) }
        assertContentEquals(doubleArrayOf(2.0, 1.0, 1.0, 1.0), buf64)
    }

    @Test
    fun zetaDistributionsCanBeCompared() {
        assertEquals(Zeta.new(2.0), Zeta.new(2.0))
    }
}
