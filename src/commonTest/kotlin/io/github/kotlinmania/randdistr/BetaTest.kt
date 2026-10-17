// port-lint: tests src/beta.rs
package io.github.kotlinmania.randdistr

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse

class BetaTest {
    @Test
    fun testBeta() {
        val beta = Beta.new(1.0, 2.0).unwrap()
        val rng = testRng(201uL)
        repeat(1000) {
            beta.sample(rng)
        }
    }

    @Test
    fun testBetaInvalidDof() {
        assertFailsWith<IllegalStateException> {
            Beta.new(0.0, 0.0).unwrap()
        }
    }

    @Test
    fun testBetaSmallParam() {
        val beta = Beta.new(1e-3, 1e-3).unwrap()
        val rng = testRng(206uL)
        for (i in 0 until 1000) {
            assertFalse(beta.sample(rng).isNaN(), "failed at i=$i")
        }
    }

    @Test
    fun betaDistributionsCanBeCompared() {
        assertEquals(Beta.new(1.0, 2.0), Beta.new(1.0, 2.0))
    }
}
