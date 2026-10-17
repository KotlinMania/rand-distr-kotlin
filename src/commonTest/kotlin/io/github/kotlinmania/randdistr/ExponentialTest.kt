// port-lint: tests src/exponential.rs
package io.github.kotlinmania.randdistr

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ExponentialTest {
    @Test
    fun testExp() {
        val exp = Exp.new(10.0).unwrap()
        val rng = testRng(221uL)
        repeat(1000) {
            assertTrue(exp.sample(rng) >= 0.0)
        }
    }

    @Test
    fun testZero() {
        val d = Exp.new(0.0).unwrap()
        assertEquals(Double.POSITIVE_INFINITY, d.sample(testRng(21uL)))
    }

    @Test
    fun testExpInvalidLambdaNeg() {
        assertFailsWith<IllegalStateException> {
            Exp.new(-10.0).unwrap()
        }
    }

    @Test
    fun testExpInvalidLambdaNan() {
        assertFailsWith<IllegalStateException> {
            Exp.new(Double.NaN).unwrap()
        }
    }

    @Test
    fun exponentialDistributionsCanBeCompared() {
        assertEquals(Exp.new(1.0), Exp.new(1.0))
    }
}
