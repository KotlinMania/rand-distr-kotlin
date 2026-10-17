// port-lint: source src/gumbel.rs
package io.github.kotlinmania.randdistr

import io.github.kotlinmania.rand.Rng
import io.github.kotlinmania.rand.distr.Distribution
import kotlin.math.ln

/**
 * Error type returned from [Gumbel.new].
 */
public enum class GumbelError {
    /** `location` is `nan` or `inf`. */
    LocationNotFinite,

    /** `scale <= 0` or `nan` or `inf`. */
    ScaleTooSmall,
    ;

    override fun toString(): String =
        when (this) {
            LocationNotFinite -> "location is not finite in Gumbel distribution"
            ScaleTooSmall -> "scale is not positive and finite in Gumbel distribution"
        }
}

/**
 * The Gumbel distribution `Gumbel(location, scale)`.
 */
public data class Gumbel internal constructor(
    private val location: Double,
    private val scale: Double,
    public val error: GumbelError? = null,
) : Distribution<Double> {
    public companion object {
        /**
         * Construct a new `Gumbel` distribution with given `location` and `scale`.
         */
        public fun new(
            location: Double,
            scale: Double,
        ): Gumbel {
            if (!location.isFinite()) {
                return Gumbel(0.0, 0.0, GumbelError.LocationNotFinite)
            }
            if (!(scale > 0.0) || scale.isInfinite()) {
                return Gumbel(0.0, 0.0, GumbelError.ScaleTooSmall)
            }
            return Gumbel(location, scale, null)
        }
    }

    public fun isOk(): Boolean = error == null

    public fun isErr(): Boolean = error != null

    public fun unwrap(): Gumbel {
        if (error != null) {
            throw IllegalStateException(error.toString())
        }
        return this
    }

    public fun unwrapErr(): GumbelError = error ?: throw IllegalStateException("Called unwrapErr on an Ok value")

    override fun sample(rng: Rng): Double {
        val x = rng.sampleOpenClosed01()
        return location - scale * ln(-ln(x))
    }
}
