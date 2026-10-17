// port-lint: source src/inverse_gaussian.rs
package io.github.kotlinmania.randdistr

import io.github.kotlinmania.rand.Rng
import io.github.kotlinmania.rand.distr.Distribution
import kotlin.math.sqrt

/**
 * Error type returned from [InverseGaussian.new].
 */
public enum class InverseGaussianError {
    /** `mean <= 0` or `nan`. */
    MeanNegativeOrNull,

    /** `shape <= 0` or `nan`. */
    ShapeNegativeOrNull,
    ;

    override fun toString(): String =
        when (this) {
            MeanNegativeOrNull -> "mean is not positive in inverse Gaussian distribution"
            ShapeNegativeOrNull -> "shape is not positive in inverse Gaussian distribution"
        }
}

/**
 * The inverse Gaussian distribution `InverseGaussian(mean, shape)`.
 */
public data class InverseGaussian internal constructor(
    private val mean: Double,
    private val shape: Double,
    public val error: InverseGaussianError? = null,
) : Distribution<Double> {
    public companion object {
        /**
         * Construct a new `InverseGaussian` distribution with the given `mean` and `shape`.
         */
        public fun new(
            mean: Double,
            shape: Double,
        ): InverseGaussian {
            if (!(mean > 0.0)) {
                return InverseGaussian(0.0, 0.0, InverseGaussianError.MeanNegativeOrNull)
            }
            if (!(shape > 0.0)) {
                return InverseGaussian(0.0, 0.0, InverseGaussianError.ShapeNegativeOrNull)
            }
            return InverseGaussian(mean, shape, null)
        }
    }

    public fun isOk(): Boolean = error == null

    public fun isErr(): Boolean = error != null

    public fun unwrap(): InverseGaussian {
        if (error != null) {
            throw IllegalStateException(error.toString())
        }
        return this
    }

    public fun unwrapErr(): InverseGaussianError = error ?: throw IllegalStateException("Called unwrapErr on an Ok value")

    override fun sample(rng: Rng): Double {
        val mu = mean
        val l = shape
        val v = rng.sample(StandardNormal)
        val y = mu * v * v
        val x = mu + 0.5 * mu / l * (y - sqrt(4.0 * l * y + y * y))
        val u = rng.randomDouble()
        if (u <= mu / (mu + x)) {
            return x
        }
        return mu * mu / x
    }
}
