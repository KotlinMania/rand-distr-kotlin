// port-lint: source src/cauchy.rs
package io.github.kotlinmania.randdistr

import io.github.kotlinmania.rand.Rng
import io.github.kotlinmania.rand.distr.Distribution
import kotlin.math.PI
import kotlin.math.tan

/**
 * Error type returned from [Cauchy.new].
 */
public enum class CauchyError {
    /** `scale <= 0` or `nan`. */
    ScaleTooSmall,
    ;

    override fun toString(): String =
        when (this) {
            ScaleTooSmall -> "scale is not positive in Cauchy distribution"
        }
}

/**
 * The Cauchy distribution `Cauchy(median, scale)`.
 */
public data class Cauchy internal constructor(
    private val median: Double,
    private val scale: Double,
    public val error: CauchyError? = null,
) : Distribution<Double> {
    public companion object {
        /**
         * Construct a new `Cauchy` with the given shape parameters `median` the
         * peak location and `scale` the scale factor.
         */
        public fun new(
            median: Double,
            scale: Double,
        ): Cauchy {
            if (!(scale > 0.0)) {
                return Cauchy(0.0, 0.0, CauchyError.ScaleTooSmall)
            }
            return Cauchy(median, scale, null)
        }
    }

    public fun isOk(): Boolean = error == null

    public fun isErr(): Boolean = error != null

    public fun unwrap(): Cauchy {
        if (error != null) {
            throw IllegalStateException(error.toString())
        }
        return this
    }

    public fun unwrapErr(): CauchyError = error ?: throw IllegalStateException("Called unwrapErr on an Ok value")

    override fun sample(rng: Rng): Double {
        val x = rng.randomDouble()
        val compDev = tan(PI * x)
        return median + scale * compDev
    }

    internal fun sampleFloat(rng: Rng): Float {
        val x = rng.randomFloat()
        val compDev = tan(PI.toFloat() * x)
        return median.toFloat() + scale.toFloat() * compDev
    }
}
