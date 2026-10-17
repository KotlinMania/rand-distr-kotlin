// port-lint: tests src/poisson.rs
package io.github.kotlinmania.randdistr

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PoissonTest {
    @Test
    fun testPoissonInvalidLambdaZero() {
        assertFailsWith<IllegalStateException> {
            Poisson.new(0.0).unwrap()
        }
    }

    @Test
    fun testPoissonInvalidLambdaInfinity() {
        assertFailsWith<IllegalStateException> {
            Poisson.new(Double.POSITIVE_INFINITY).unwrap()
        }
    }

    @Test
    fun testPoissonInvalidLambdaNeg() {
        assertFailsWith<IllegalStateException> {
            Poisson.new(-10.0).unwrap()
        }
    }

    @Test
    fun poissonDistributionsCanBeCompared() {
        assertEquals(Poisson.new(1.0), Poisson.new(1.0))
    }
}
