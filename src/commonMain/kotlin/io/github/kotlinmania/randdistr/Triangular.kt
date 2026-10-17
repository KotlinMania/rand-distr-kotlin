// port-lint: source src/triangular.rs
package io.github.kotlinmania.randdistr

import io.github.kotlinmania.rand.Rng
import io.github.kotlinmania.rand.distr.Distribution
import kotlin.math.sqrt

/**
 * Error type returned from [Triangular.new].
 */
public enum class TriangularError {
    /** `max < min` or `min` or `max` is NaN, or `max - min` overflows. */
    RangeTooSmall,

    /** `mode < min` or `mode > max` or `mode` is NaN. */
    ModeRange,
    ;

    override fun toString(): String =
        when (this) {
            RangeTooSmall -> "requirement min <= max is not met in triangular distribution"
            ModeRange -> "mode is outside [min, max] in triangular distribution"
        }
}

/**
 * The triangular distribution.
 */
public data class Triangular internal constructor(
    private val min: Double,
    private val max: Double,
    private val mode: Double,
    public val error: TriangularError? = null,
) : Distribution<Double> {
    public companion object {
        /**
         * Construct a new `Triangular` with minimum `min`, maximum `max` and mode `mode`.
         */
        public fun new(
            min: Double,
            max: Double,
            mode: Double,
        ): Triangular {
            if (!(max >= min)) {
                return Triangular(0.0, 0.0, 0.0, TriangularError.RangeTooSmall)
            }
            if (!(mode >= min && max >= mode)) {
                return Triangular(0.0, 0.0, 0.0, TriangularError.ModeRange)
            }
            if ((max - min).isInfinite()) {
                return Triangular(0.0, 0.0, 0.0, TriangularError.RangeTooSmall)
            }
            return Triangular(min, max, mode, null)
        }
    }

    public fun isOk(): Boolean = error == null

    public fun isErr(): Boolean = error != null

    public fun unwrap(): Triangular {
        if (error != null) {
            throw IllegalStateException(error.toString())
        }
        return this
    }

    public fun unwrapErr(): TriangularError = error ?: throw IllegalStateException("Called unwrapErr on an Ok value")

    override fun sample(rng: Rng): Double {
        val f = rng.randomDouble()
        val diffModeMin = mode - min
        val range = max - min
        val fRange = f * range
        if (fRange < diffModeMin) {
            return min + sqrt(fRange * diffModeMin)
        }
        return max - sqrt((range - fRange) * (max - mode))
    }
}
