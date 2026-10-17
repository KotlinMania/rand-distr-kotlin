// port-lint: tests src/unit_circle.rs
package io.github.kotlinmania.randdistr

import kotlin.test.Test

class UnitCircleTest {
    @Test
    fun norm() {
        val rng = testRng(1uL)
        repeat(1000) {
            val x = UnitCircle.sample(rng)
            assertAlmostEq(x[0] * x[0] + x[1] * x[1], 1.0, 1e-15)
        }
    }
}
