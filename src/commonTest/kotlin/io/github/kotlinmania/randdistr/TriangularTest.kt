// port-lint: tests src/triangular.rs
package io.github.kotlinmania.randdistr

import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TriangularTest {
    @Test
    fun testTriangular() {
        val halfRng = StepRng(0x8000_0000_0000_0000uL, 0uL)
        assertEquals(0.5, halfRng.randomDouble())
        val cases =
            listOf(
                doubleArrayOf(-1.0, 1.0, 0.0, 0.0),
                doubleArrayOf(1.0, 2.0, 1.0, 2.0 - sqrt(0.5)),
                doubleArrayOf(5.0, 25.0, 25.0, 5.0 + sqrt(200.0)),
                doubleArrayOf(1e-5, 1e5, 1e-3, 1e5 - sqrt(4999999949.5)),
                doubleArrayOf(0.0, 1.0, 0.9, sqrt(0.45)),
                doubleArrayOf(-4.0, -0.5, -2.0, -4.0 + sqrt(3.5)),
            )
        for (case in cases) {
            val min = case[0]
            val max = case[1]
            val mode = case[2]
            val median = case[3]
            val distr = Triangular.new(min, max, mode).unwrap()
            assertEquals(median, distr.sample(halfRng))
        }

        val invalidCases =
            listOf(
                Triple(-1.0, 1.0, 2.0),
                Triple(-1.0, 1.0, -2.0),
                Triple(2.0, 1.0, 1.0),
            )
        for ((min, max, mode) in invalidCases) {
            assertTrue(Triangular.new(min, max, mode).isErr())
        }
    }

    @Test
    fun triangularDistributionsCanBeCompared() {
        assertEquals(
            Triangular.new(1.0, 3.0, 2.0),
            Triangular.new(1.0, 3.0, 2.0),
        )
    }
}
