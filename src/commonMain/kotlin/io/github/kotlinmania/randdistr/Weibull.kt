// port-lint: source src/weibull.rs
package io.github.kotlinmania.randdistr

import io.github.kotlinmania.rand.Rng
import io.github.kotlinmania.rand.distr.Distribution
import kotlin.math.ln
import kotlin.math.pow

/**
 * Error type returned from [Weibull.new].
 */
public enum class WeibullError {
    /** `scale < 0` or `nan`. */
    ScaleTooSmall,

    /** `shape <= 0` or `nan` or `1 / shape == 0`. */
    ShapeTooSmall,
    ;

    override fun toString(): String =
        when (this) {
            ScaleTooSmall -> "scale is negative or NaN in Weibull distribution"
            ShapeTooSmall -> "shape is not positive or is too large in Weibull distribution"
        }
}

/**
 * The Weibull distribution `Weibull(scale, shape)`.
 */
public data class Weibull internal constructor(
    private val invShape: Double,
    private val scale: Double,
    public val error: WeibullError? = null,
) : Distribution<Double> {
    public companion object {
        /**
         * Construct a new `Weibull` distribution with given `scale` and `shape`.
         */
        public fun new(
            scale: Double,
            shape: Double,
        ): Weibull {
            if (!(scale >= 0.0)) {
                return Weibull(0.0, 0.0, WeibullError.ScaleTooSmall)
            }
            if (!(shape > 0.0)) {
                return Weibull(0.0, 0.0, WeibullError.ShapeTooSmall)
            }
            val invShape = 1.0 / shape
            if (invShape == 0.0) {
                return Weibull(0.0, 0.0, WeibullError.ShapeTooSmall)
            }
            return Weibull(invShape, scale, null)
        }
    }

    public fun isOk(): Boolean = error == null

    public fun isErr(): Boolean = error != null

    public fun unwrap(): Weibull {
        if (error != null) {
            throw IllegalStateException(error.toString())
        }
        return this
    }

    public fun unwrapErr(): WeibullError = error ?: throw IllegalStateException("Called unwrapErr on an Ok value")

    override fun sample(rng: Rng): Double {
        val x = rng.sampleOpenClosed01()
        return scale * (-ln(x)).pow(invShape)
    }

    internal fun sampleFloat(rng: Rng): Float {
        val x = rng.sampleOpenClosed01Float()
        return scale.toFloat() * (-ln(x)).pow(invShape.toFloat())
    }
}
