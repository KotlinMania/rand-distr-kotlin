// port-lint: tests src/normal.rs
package io.github.kotlinmania.randdistr

import kotlin.math.E
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NormalTest {
    @Test
    fun testNormal() {
        val norm = Normal.new(10.0, 10.0).unwrap()
        val rng = testRng(210uL)
        repeat(1000) {
            norm.sample(rng)
        }
    }

    @Test
    fun testNormalCv() {
        val norm = Normal.fromMeanCv(1024.0, 1.0 / 256.0).unwrap()
        assertEquals(1024.0, norm.mean())
        assertEquals(4.0, norm.stdDev())
    }

    @Test
    fun testNormalInvalidSd() {
        assertTrue(Normal.fromMeanCv(10.0, -1.0).isErr())
    }

    @Test
    fun testLogNormal() {
        val lnorm = LogNormal.new(10.0, 10.0).unwrap()
        val rng = testRng(211uL)
        repeat(1000) {
            lnorm.sample(rng)
        }
    }

    @Test
    fun testLogNormalCv() {
        val lnorm1 = LogNormal.fromMeanCv(0.0, 0.0).unwrap()
        assertEquals(Double.NEGATIVE_INFINITY, lnorm1.norm.mean())
        assertEquals(0.0, lnorm1.norm.stdDev())

        val lnorm2 = LogNormal.fromMeanCv(1.0, 0.0).unwrap()
        assertEquals(0.0, lnorm2.norm.mean())
        assertEquals(0.0, lnorm2.norm.stdDev())

        val e = E
        val lnorm3 = LogNormal.fromMeanCv(sqrt(e), sqrt(e - 1.0)).unwrap()
        assertAlmostEq(lnorm3.norm.mean(), 0.0, 2e-16)
        assertAlmostEq(lnorm3.norm.stdDev(), 1.0, 2e-16)

        val lnorm4 = LogNormal.fromMeanCv(e.pow(1.5), sqrt(e - 1.0)).unwrap()
        assertAlmostEq(lnorm4.norm.mean(), 1.0, 1e-15)
        assertEquals(1.0, lnorm4.norm.stdDev())
    }

    @Test
    fun testLogNormalInvalidSd() {
        assertTrue(LogNormal.fromMeanCv(-1.0, 1.0).isErr())
        assertTrue(LogNormal.fromMeanCv(0.0, 1.0).isErr())
        assertTrue(LogNormal.fromMeanCv(1.0, -1.0).isErr())
    }

    @Test
    fun normalDistributionsCanBeCompared() {
        assertEquals(Normal.new(1.0, 2.0), Normal.new(1.0, 2.0))
    }

    @Test
    fun logNormalDistributionsCanBeCompared() {
        assertEquals(LogNormal.new(1.0, 2.0), LogNormal.new(1.0, 2.0))
    }
}
