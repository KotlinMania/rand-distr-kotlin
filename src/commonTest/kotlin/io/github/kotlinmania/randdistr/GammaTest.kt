// port-lint: tests src/gamma.rs
package io.github.kotlinmania.randdistr

import kotlin.test.Test
import kotlin.test.assertEquals

class GammaTest {
    @Test
    fun gammaDistributionsCanBeCompared() {
        assertEquals(Gamma.new(1.0, 2.0), Gamma.new(1.0, 2.0))
    }
}
