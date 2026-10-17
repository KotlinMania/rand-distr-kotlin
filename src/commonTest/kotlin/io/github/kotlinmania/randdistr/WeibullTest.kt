// port-lint: tests src/weibull.rs
package io.github.kotlinmania.randdistr

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class WeibullTest {
    @Test
    fun invalid() {
        assertFailsWith<IllegalStateException> {
            Weibull.new(0.0, 0.0).unwrap()
        }
    }

    @Test
    fun sample() {
        val scale = 1.0
        val shape = 2.0
        val d = Weibull.new(scale, shape).unwrap()
        val rng = testRng(1uL)
        repeat(1000) {
            val r = d.sample(rng)
            assertTrue(r >= 0.0)
        }
    }

    @Test
    fun valueStability() {
        val distr32 = Weibull.new(1.0, 1.0).unwrap()
        val rng32 = testRng(213uL)
        val buf32 = FloatArray(4) { distr32.sampleFloat(rng32) }
        assertContentEquals(
            floatArrayOf(0.041495778f, 0.7531094f, 1.4189332f, 0.38386202f),
            buf32,
        )

        val distr64 = Weibull.new(2.0, 0.5).unwrap()
        val rng64 = testRng(213uL)
        val buf64 = DoubleArray(4) { distr64.sample(rng64) }
        assertContentEquals(
            doubleArrayOf(
                1.1343478702739669,
                0.29470010050655226,
                0.7556151370284702,
                7.877212340241561,
            ),
            buf64,
        )
    }

    @Test
    fun weibullDistributionsCanBeCompared() {
        assertEquals(Weibull.new(1.0, 2.0), Weibull.new(1.0, 2.0))
    }
}
