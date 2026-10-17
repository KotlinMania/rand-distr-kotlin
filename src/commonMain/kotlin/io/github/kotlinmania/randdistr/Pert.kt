// port-lint: source src/pert.rs
package io.github.kotlinmania.randdistr

import io.github.kotlinmania.rand.Rng
import io.github.kotlinmania.rand.distr.Distribution

/**
 * Error type returned from [Pert] constructors.
 */
public enum class PertError {
    /** `max < min` or `min` or `max` is NaN. */
    RangeTooSmall,

    /** `mode < min` or `mode > max` or `mode` is NaN. */
    ModeRange,

    /** `shape < 0` or `shape` is NaN. */
    ShapeTooSmall,
    ;

    override fun toString(): String =
        when (this) {
            RangeTooSmall -> "requirement min < max is not met in PERT distribution"
            ModeRange -> "mode is outside [min, max] in PERT distribution"
            ShapeTooSmall -> "shape < 0 or is NaN in PERT distribution"
        }
}

/**
 * Struct used to build a [Pert].
 */
public data class PertBuilder(
    private val min: Double,
    private val max: Double,
    private val shape: Double,
) {
    /**
     * Set the shape parameter.
     */
    public fun withShape(shape: Double): PertBuilder = PertBuilder(min, max, shape)

    /**
     * Specify the mean.
     */
    public fun withMean(mean: Double): Pert {
        val two = 2.0
        val mode = ((shape + two) * mean - min - max) / shape
        return withMode(mode)
    }

    /**
     * Specify the mode.
     */
    public fun withMode(mode: Double): Pert {
        val dummyBeta = Beta.new(1.0, 1.0)
        if (!(max > min)) {
            return Pert(0.0, 0.0, dummyBeta, PertError.RangeTooSmall)
        }
        if (!(mode >= min && max >= mode)) {
            return Pert(0.0, 0.0, dummyBeta, PertError.ModeRange)
        }
        if (!(shape >= 0.0)) {
            return Pert(0.0, 0.0, dummyBeta, PertError.ShapeTooSmall)
        }
        val range = max - min
        val v = 1.0 + shape * (mode - min) / range
        val w = 1.0 + shape * (max - mode) / range
        val beta = Beta.new(v, w)
        if (beta.error != null) {
            return Pert(0.0, 0.0, dummyBeta, PertError.RangeTooSmall)
        }
        return Pert(min, range, beta, null)
    }
}

/**
 * The PERT distribution `PERT(min, max, mode, shape)`.
 */
public data class Pert internal constructor(
    private val min: Double,
    private val range: Double,
    private val beta: Beta,
    public val error: PertError? = null,
) : Distribution<Double> {
    public companion object {
        /**
         * Construct a PERT distribution with defined `min`, `max`.
         */
        public fun new(
            min: Double,
            max: Double,
        ): PertBuilder = PertBuilder(min, max, 4.0)
    }

    public fun isOk(): Boolean = error == null

    public fun isErr(): Boolean = error != null

    public fun unwrap(): Pert {
        if (error != null) {
            throw IllegalStateException(error.toString())
        }
        return this
    }

    public fun unwrapErr(): PertError = error ?: throw IllegalStateException("Called unwrapErr on an Ok value")

    override fun sample(rng: Rng): Double = beta.sample(rng) * range + min
}
