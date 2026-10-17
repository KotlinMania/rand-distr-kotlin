// port-lint: source src/chi_squared.rs
package io.github.kotlinmania.randdistr

import io.github.kotlinmania.rand.Rng
import io.github.kotlinmania.rand.distr.Distribution

/**
 * Error type returned from [ChiSquared.new] and [StudentT.new].
 */
public enum class ChiSquaredError {
    /** `0.5 * k <= 0` or `nan`. */
    DoFTooSmall,
    ;

    override fun toString(): String =
        when (this) {
            DoFTooSmall -> "degrees of freedom k is not positive in chi-squared distribution"
        }
}

internal sealed class ChiSquaredRepr {
    internal data object DoFExactlyOne : ChiSquaredRepr()

    internal data class DoFAnythingElse(
        val gamma: Gamma,
    ) : ChiSquaredRepr()
}

/**
 * The chi-squared distribution `χ²(k)`, where `k` is the degrees of
 * freedom.
 */
public data class ChiSquared internal constructor(
    private val repr: ChiSquaredRepr,
    public val error: ChiSquaredError? = null,
) : Distribution<Double> {
    public companion object {
        /**
         * Create a new chi-squared distribution with degrees-of-freedom
         * `k`.
         */
        public fun new(k: Double): ChiSquared {
            val repr =
                if (k == 1.0) {
                    ChiSquaredRepr.DoFExactlyOne
                } else {
                    if (!(0.5 * k > 0.0)) {
                        return ChiSquared(ChiSquaredRepr.DoFExactlyOne, ChiSquaredError.DoFTooSmall)
                    }
                    val gamma = Gamma.new(0.5 * k, 2.0)
                    if (gamma.error != null) {
                        return ChiSquared(ChiSquaredRepr.DoFExactlyOne, ChiSquaredError.DoFTooSmall)
                    }
                    ChiSquaredRepr.DoFAnythingElse(gamma)
                }
            return ChiSquared(repr, null)
        }
    }

    public fun isOk(): Boolean = error == null

    public fun isErr(): Boolean = error != null

    public fun unwrap(): ChiSquared {
        if (error != null) {
            throw IllegalStateException(error.toString())
        }
        return this
    }

    public fun unwrapErr(): ChiSquaredError = error ?: throw IllegalStateException("Called unwrapErr on an Ok value")

    override fun sample(rng: Rng): Double =
        when (val r = repr) {
            is ChiSquaredRepr.DoFExactlyOne -> {
                val norm = rng.sample(StandardNormal)
                norm * norm
            }

            is ChiSquaredRepr.DoFAnythingElse -> {
                r.gamma.sample(rng)
            }
        }
}
