// port-lint: tests src/pareto.rs
package io.github.kotlinmania.randdistr

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ParetoTest {
    @Test
    fun invalid() {
        assertFailsWith<IllegalStateException> {
            Pareto.new(0.0, 0.0).unwrap()
        }
    }

    @Test
    fun sample() {
        val scale = 1.0
        val shape = 2.0
        val d = Pareto.new(scale, shape).unwrap()
        val rng = testRng(1uL)
        repeat(1000) {
            val r = d.sample(rng)
            assertTrue(r >= scale)
        }
    }

    @Test
    fun valueStability() {
        val distr32 = Pareto.new(1.0, 1.0).unwrap()
        val rng32 = testRng(213uL)
        val expected32 = floatArrayOf(1.0423688f, 2.1235929f, 4.132709f, 1.4679428f)
        for (v in expected32) {
            val x = distr32.sampleFloat(rng32)
            assertAlmostEq(x.toDouble(), v.toDouble(), 1e-6)
        }

        val distr64 = Pareto.new(2.0, 0.5).unwrap()
        val rng64 = testRng(213uL)
        val expected64 =
            doubleArrayOf(
                9.019295276219136,
                4.3097126018270595,
                6.837815045397157,
                105.8826669383772,
            )
        for (v in expected64) {
            val x = distr64.sample(rng64)
            assertAlmostEq(x, v, 1e-14)
        }
    }

    @Test
    fun paretoDistributionsCanBeCompared() {
        assertEquals(Pareto.new(1.0, 2.0), Pareto.new(1.0, 2.0))
    }
}
