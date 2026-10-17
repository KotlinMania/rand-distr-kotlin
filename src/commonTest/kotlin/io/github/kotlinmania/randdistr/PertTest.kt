// port-lint: tests src/pert.rs
package io.github.kotlinmania.randdistr

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PertTest {
    @Test
    fun testPert() {
        val validCases =
            listOf(
                Triple(-1.0, 1.0, 0.0),
                Triple(1.0, 2.0, 1.0),
                Triple(5.0, 25.0, 25.0),
            )
        for ((min, max, mode) in validCases) {
            Pert.new(min, max).withMode(mode).unwrap()
        }

        val invalidCases =
            listOf(
                Triple(-1.0, 1.0, 2.0),
                Triple(-1.0, 1.0, -2.0),
                Triple(2.0, 1.0, 1.0),
            )
        for ((min, max, mode) in invalidCases) {
            assertTrue(Pert.new(min, max).withMode(mode).isErr())
        }
    }

    @Test
    fun distributionsCanBeCompared() {
        val min = 1.0
        val mode = 2.0
        val max = 3.0
        val shape = 4.0
        val p1 = Pert.new(min, max).withMode(mode).unwrap()
        val mean = (min + shape * mode + max) / (shape + 2.0)
        val p2 = Pert.new(min, max).withMean(mean).unwrap()
        assertEquals(p1, p2)
    }

    @Test
    fun modeAlmostHalfRange() {
        assertTrue(Pert.new(0.0, 0.48258883).withMode(0.24129441).isOk())
    }

    @Test
    fun almostSymmetricAboutZero() {
        val distr = Pert.new(-10.0, 10.0).withMode(1.1920929e-7)
        assertTrue(distr.isOk())
    }

    @Test
    fun almostSymmetric() {
        val distr = Pert.new(0.0, 2.0).withMode(1.0 + 1.1920929e-7)
        assertTrue(distr.isOk())
    }
}
