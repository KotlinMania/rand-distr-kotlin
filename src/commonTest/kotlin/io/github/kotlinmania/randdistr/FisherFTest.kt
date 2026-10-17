// port-lint: tests src/fisher_f.rs
package io.github.kotlinmania.randdistr

import kotlin.test.Test
import kotlin.test.assertEquals

class FisherFTest {
    @Test
    fun testF() {
        val f = FisherF.new(2.0, 32.0).unwrap()
        val rng = testRng(204uL)
        repeat(1000) {
            f.sample(rng)
        }
    }

    @Test
    fun fisherFDistributionsCanBeCompared() {
        assertEquals(FisherF.new(1.0, 2.0), FisherF.new(1.0, 2.0))
    }
}
