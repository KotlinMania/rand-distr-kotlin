// port-lint: source src/frechet.rs
package io.github.kotlinmania.randdistr

import io.github.kotlinmania.rand.Rng
import io.github.kotlinmania.rand.distr.Distribution
import kotlin.math.ln
import kotlin.math.pow

/**
 * Error type returned from [Frechet.new].
 */
public enum class FrechetError {
    /** `location` is `nan` or `inf`. */
    LocationNotFinite,

    /** `scale <= 0` or `nan` or `inf`. */
    ScaleTooSmall,

    /** `shape <= 0` or `nan` or `inf`. */
    ShapeTooSmall,
    ;

    override fun toString(): String =
        when (this) {
            LocationNotFinite -> "location is not finite in Frechet distribution"
            ScaleTooSmall -> "scale is not positive and finite in Frechet distribution"
            ShapeTooSmall -> "shape is not positive and finite in Frechet distribution"
        }
}

/**
 * The Fréchet distribution `Frechet(location, scale, shape)`.
 */
public data class Frechet internal constructor(
    private val location: Double,
    private val scale: Double,
    private val shape: Double,
    public val error: FrechetError? = null,
) : Distribution<Double> {
    public companion object {
        /**
         * Construct a new `Frechet` distribution with given `location`, `scale`, and `shape`.
         */
        public fun new(
            location: Double,
            scale: Double,
            shape: Double,
        ): Frechet {
            if (!location.isFinite()) {
                return Frechet(0.0, 0.0, 0.0, FrechetError.LocationNotFinite)
            }
            if (!(scale > 0.0) || scale.isInfinite()) {
                return Frechet(0.0, 0.0, 0.0, FrechetError.ScaleTooSmall)
            }
            if (!(shape > 0.0) || shape.isInfinite()) {
                return Frechet(0.0, 0.0, 0.0, FrechetError.ShapeTooSmall)
            }
            return Frechet(location, scale, shape, null)
        }
    }

    public fun isOk(): Boolean = error == null

    public fun isErr(): Boolean = error != null

    public fun unwrap(): Frechet {
        if (error != null) {
            throw IllegalStateException(error.toString())
        }
        return this
    }

    public fun unwrapErr(): FrechetError = error ?: throw IllegalStateException("Called unwrapErr on an Ok value")

    override fun sample(rng: Rng): Double {
        val x = rng.sampleOpenClosed01()
        return location + scale * (-ln(x)).pow(-1.0 / shape)
    }
}
