// port-lint: source src/normal_inverse_gaussian.rs
package io.github.kotlinmania.randdistr

import io.github.kotlinmania.rand.Rng
import io.github.kotlinmania.rand.distr.Distribution
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Error type returned from [NormalInverseGaussian.new].
 */
public enum class NormalInverseGaussianError {
    /** `alpha <= 0` or `nan`. */
    AlphaNegativeOrNull,

    /** `|beta| >= alpha` or `nan`. */
    AbsoluteBetaGreaterThanAlpha,
    ;

    override fun toString(): String =
        when (this) {
            AlphaNegativeOrNull -> {
                "alpha <= 0 or is NaN in normal inverse Gaussian distribution"
            }

            AbsoluteBetaGreaterThanAlpha -> {
                "|beta| >= alpha or is NaN in normal inverse Gaussian distribution"
            }
        }
}

/**
 * The normal-inverse Gaussian distribution `NormalInverseGaussian(alpha, beta)`.
 */
public data class NormalInverseGaussian internal constructor(
    private val alpha: Double,
    private val beta: Double,
    private val inverseGaussian: InverseGaussian,
    public val error: NormalInverseGaussianError? = null,
) : Distribution<Double> {
    public companion object {
        /**
         * Construct a new `NormalInverseGaussian` distribution with the given `alpha` (tail heaviness)
         * and `beta` (asymmetry) parameters.
         */
        public fun new(
            alpha: Double,
            beta: Double,
        ): NormalInverseGaussian {
            val dummyIg = InverseGaussian.new(1.0, 1.0)
            if (!(alpha > 0.0)) {
                return NormalInverseGaussian(0.0, 0.0, dummyIg, NormalInverseGaussianError.AlphaNegativeOrNull)
            }
            if (!(abs(beta) < alpha)) {
                return NormalInverseGaussian(
                    0.0,
                    0.0,
                    dummyIg,
                    NormalInverseGaussianError.AbsoluteBetaGreaterThanAlpha,
                )
            }
            val gamma = sqrt(alpha * alpha - beta * beta)
            val mu = 1.0 / gamma
            val ig = InverseGaussian.new(mu, 1.0).unwrap()
            return NormalInverseGaussian(alpha, beta, ig, null)
        }
    }

    public fun isOk(): Boolean = error == null

    public fun isErr(): Boolean = error != null

    public fun unwrap(): NormalInverseGaussian {
        if (error != null) {
            throw IllegalStateException(error.toString())
        }
        return this
    }

    public fun unwrapErr(): NormalInverseGaussianError = error ?: throw IllegalStateException("Called unwrapErr on an Ok value")

    override fun sample(rng: Rng): Double {
        val invGauss = rng.sample(inverseGaussian)
        return beta * invGauss + sqrt(invGauss) * rng.sample(StandardNormal)
    }
}
