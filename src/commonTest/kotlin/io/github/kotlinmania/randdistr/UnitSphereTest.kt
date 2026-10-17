// port-lint: tests src/unit_sphere.rs
package io.github.kotlinmania.randdistr

import kotlin.test.Test

class UnitSphereTest {
    @Test
    fun norm() {
        val rng = testRng(1uL)
        repeat(1000) {
            val x = UnitSphere.sample(rng)
            assertAlmostEq(x[0] * x[0] + x[1] * x[1] + x[2] * x[2], 1.0, 1e-15)
        }
    }
}
