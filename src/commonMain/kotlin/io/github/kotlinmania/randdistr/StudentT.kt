// port-lint: source src/student_t.rs
package io.github.kotlinmania.randdistr

import io.github.kotlinmania.rand.Rng
import io.github.kotlinmania.rand.distr.Distribution
import kotlin.math.sqrt

/**
 * The Student t distribution, `t(nu)`, where `nu` is the degrees of
 * freedom.
 */
public data class StudentT internal constructor(
    private val chi: ChiSquared,
    private val dof: Double,
    public val error: ChiSquaredError? = null,
) : Distribution<Double> {
    public companion object {
        /**
         * Create a new `StudentT` distribution with `n` degrees of freedom.
         */
        public fun new(n: Double): StudentT {
            val chi = ChiSquared.new(n)
            if (chi.error != null) {
                return StudentT(chi, n, chi.error)
            }
            return StudentT(chi, n, null)
        }
    }

    public fun isOk(): Boolean = error == null

    public fun isErr(): Boolean = error != null

    public fun unwrap(): StudentT {
        if (error != null) {
            throw IllegalStateException(error.toString())
        }
        return this
    }

    public fun unwrapErr(): ChiSquaredError = error ?: throw IllegalStateException("Called unwrapErr on an Ok value")

    override fun sample(rng: Rng): Double {
        val norm = rng.sample(StandardNormal)
        return norm * sqrt(dof / chi.sample(rng))
    }
}
