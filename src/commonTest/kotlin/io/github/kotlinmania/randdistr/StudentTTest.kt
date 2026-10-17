// port-lint: tests src/student_t.rs
package io.github.kotlinmania.randdistr

import kotlin.test.Test
import kotlin.test.assertEquals

class StudentTTest {
    @Test
    fun testT() {
        val t = StudentT.new(11.0).unwrap()
        val rng = testRng(205uL)
        repeat(1000) {
            t.sample(rng)
        }
    }

    @Test
    fun studentTDistributionsCanBeCompared() {
        assertEquals(StudentT.new(1.0), StudentT.new(1.0))
    }
}
