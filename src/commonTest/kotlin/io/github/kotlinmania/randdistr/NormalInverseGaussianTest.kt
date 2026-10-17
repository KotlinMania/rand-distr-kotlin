// port-lint: tests src/normal_inverse_gaussian.rs
package io.github.kotlinmania.randdistr

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NormalInverseGaussianTest {
    @Test
    fun testNormalInverseGaussian() {
        val normInvGauss = NormalInverseGaussian.new(2.0, 1.0).unwrap()
        val rng = testRng(210uL)
        repeat(1000) {
            normInvGauss.sample(rng)
        }
    }

    @Test
    fun testNormalInverseGaussianInvalidParam() {
        assertTrue(NormalInverseGaussian.new(-1.0, 1.0).isErr())
        assertTrue(NormalInverseGaussian.new(-1.0, -1.0).isErr())
        assertTrue(NormalInverseGaussian.new(1.0, 2.0).isErr())
        assertTrue(NormalInverseGaussian.new(2.0, 1.0).isOk())
    }

    @Test
    fun normalInverseGaussianDistributionsCanBeCompared() {
        assertEquals(
            NormalInverseGaussian.new(1.0, 2.0),
            NormalInverseGaussian.new(1.0, 2.0),
        )
    }
}
