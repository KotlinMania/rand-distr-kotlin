// port-lint: source src/fisher_f.rs
package io.github.kotlinmania.randdistr

import io.github.kotlinmania.rand.Rng
import io.github.kotlinmania.rand.distr.Distribution

/**
 * Error type returned from [FisherF.new].
 */
public enum class FisherFError {
    /** `m <= 0` or `nan`. */
    MTooSmall,

    /** `n <= 0` or `nan`. */
    NTooSmall,
    ;

    override fun toString(): String =
        when (this) {
            MTooSmall -> "m is not positive in Fisher F distribution"
            NTooSmall -> "n is not positive in Fisher F distribution"
        }
}

/**
 * The Fisher F-distribution `F(m, n)`.
 */
public data class FisherF internal constructor(
    private val numer: ChiSquared,
    private val denom: ChiSquared,
    private val dofRatio: Double,
    public val error: FisherFError? = null,
) : Distribution<Double> {
    public companion object {
        /**
         * Create a new `FisherF` distribution, with the given parameter.
         */
        public fun new(
            m: Double,
            n: Double,
        ): FisherF {
            val dummyChi = ChiSquared.new(1.0)
            if (!(m > 0.0)) {
                return FisherF(dummyChi, dummyChi, 0.0, FisherFError.MTooSmall)
            }
            if (!(n > 0.0)) {
                return FisherF(dummyChi, dummyChi, 0.0, FisherFError.NTooSmall)
            }
            return FisherF(
                numer = ChiSquared.new(m).unwrap(),
                denom = ChiSquared.new(n).unwrap(),
                dofRatio = n / m,
                error = null,
            )
        }
    }

    public fun isOk(): Boolean = error == null

    public fun isErr(): Boolean = error != null

    public fun unwrap(): FisherF {
        if (error != null) {
            throw IllegalStateException(error.toString())
        }
        return this
    }

    public fun unwrapErr(): FisherFError = error ?: throw IllegalStateException("Called unwrapErr on an Ok value")

    override fun sample(rng: Rng): Double = numer.sample(rng) / denom.sample(rng) * dofRatio
}
