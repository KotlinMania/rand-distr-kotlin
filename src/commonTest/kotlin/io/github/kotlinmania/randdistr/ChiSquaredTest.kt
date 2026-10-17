// port-lint: tests src/chi_squared.rs
package io.github.kotlinmania.randdistr

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ChiSquaredTest {
    @Test
    fun testChiSquaredOne() {
        val chi = ChiSquared.new(1.0).unwrap()
        val rng = testRng(201uL)
        repeat(1000) {
            chi.sample(rng)
        }
    }

    @Test
    fun testChiSquaredSmall() {
        val chi = ChiSquared.new(0.5).unwrap()
        val rng = testRng(202uL)
        repeat(1000) {
            chi.sample(rng)
        }
    }

    @Test
    fun testChiSquaredLarge() {
        val chi = ChiSquared.new(30.0).unwrap()
        val rng = testRng(203uL)
        repeat(1000) {
            chi.sample(rng)
        }
    }

    @Test
    fun testChiSquaredInvalidDof() {
        assertFailsWith<IllegalStateException> {
            ChiSquared.new(-1.0).unwrap()
        }
    }

    @Test
    fun gammaDistributionsCanBeCompared() {
        assertEquals(Gamma.new(1.0, 2.0), Gamma.new(1.0, 2.0))
    }

    @Test
    fun chiSquaredDistributionsCanBeCompared() {
        assertEquals(ChiSquared.new(1.0), ChiSquared.new(1.0))
    }
}
