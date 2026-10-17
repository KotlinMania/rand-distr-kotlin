// port-lint: tests src/unit_disc.rs
package io.github.kotlinmania.randdistr

import kotlin.test.Test
import kotlin.test.assertTrue

class UnitDiscTest {
    @Test
    fun norm() {
        val rng = testRng(1uL)
        repeat(1000) {
            val x = UnitDisc.sample(rng)
            assertTrue(x[0] * x[0] + x[1] * x[1] <= 1.0)
        }
    }
}
