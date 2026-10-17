// port-lint: tests src/inverse_gaussian.rs
package io.github.kotlinmania.randdistr

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InverseGaussianTest {
    @Test
    fun testInverseGaussian() {
        val invGauss = InverseGaussian.new(1.0, 1.0).unwrap()
        val rng = testRng(210uL)
        repeat(1000) {
            invGauss.sample(rng)
        }
    }

    @Test
    fun testInverseGaussianInvalidParam() {
        assertTrue(InverseGaussian.new(-1.0, 1.0).isErr())
        assertTrue(InverseGaussian.new(-1.0, -1.0).isErr())
        assertTrue(InverseGaussian.new(1.0, -1.0).isErr())
        assertTrue(InverseGaussian.new(1.0, 1.0).isOk())
    }

    @Test
    fun inverseGaussianDistributionsCanBeCompared() {
        assertEquals(
            InverseGaussian.new(1.0, 2.0),
            InverseGaussian.new(1.0, 2.0),
        )
    }
}
