// port-lint: source src/skew_normal.rs
package io.github.kotlinmania.randdistr

import io.github.kotlinmania.rand.Rng
import io.github.kotlinmania.rand.distr.Distribution
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

private const val SQRT_2: Double = 1.4142135623730951

/**
 * Error type returned from [SkewNormal.new].
 */
public enum class SkewNormalError {
    /** The scale parameter is not finite or is not positive. */
    ScaleTooSmall,

    /** The shape parameter is not finite. */
    BadShape,
    ;

    override fun toString(): String =
        when (this) {
            ScaleTooSmall -> "scale is not positive and finite in SkewNormal distribution"
            BadShape -> "shape is not finite in SkewNormal distribution"
        }
}

/**
 * The skew normal distribution `SN(location, scale, shape)`.
 */
public data class SkewNormal internal constructor(
    private val locationVal: Double,
    private val scaleVal: Double,
    private val shapeVal: Double,
    public val error: SkewNormalError? = null,
) : Distribution<Double> {
    public companion object {
        /**
         * Construct, from location, scale and shape.
         */
        public fun new(
            location: Double,
            scale: Double,
            shape: Double,
        ): SkewNormal {
            if (!scale.isFinite() || !(scale > 0.0)) {
                return SkewNormal(location, scale, shape, SkewNormalError.ScaleTooSmall)
            }
            if (!shape.isFinite()) {
                return SkewNormal(location, scale, shape, SkewNormalError.BadShape)
            }
            return SkewNormal(location, scale, shape, null)
        }
    }

    /**
     * Returns the location of the distribution.
     */
    public fun location(): Double = locationVal

    /**
     * Returns the scale of the distribution.
     */
    public fun scale(): Double = scaleVal

    /**
     * Returns the shape of the distribution.
     */
    public fun shape(): Double = shapeVal

    public fun isOk(): Boolean = error == null

    public fun isErr(): Boolean = error != null

    public fun unwrap(): SkewNormal {
        if (error != null) {
            throw IllegalStateException(error.toString())
        }
        return this
    }

    public fun unwrapErr(): SkewNormalError = error ?: throw IllegalStateException("Called unwrapErr on an Ok value")

    override fun sample(rng: Rng): Double {
        fun linearMap(x: Double): Double = x * scaleVal + locationVal
        val u1 = rng.sample(StandardNormal)
        return if (shapeVal == 0.0) {
            linearMap(u1)
        } else {
            val u2 = rng.sample(StandardNormal)
            val u = max(u1, u2)
            val v = min(u1, u2)
            when (shapeVal) {
                -1.0 -> {
                    linearMap(v)
                }

                1.0 -> {
                    linearMap(u)
                }

                else -> {
                    val normalized =
                        ((1.0 + shapeVal) * u + (1.0 - shapeVal) * v) /
                            (sqrt(1.0 + shapeVal * shapeVal) * SQRT_2)
                    linearMap(normalized)
                }
            }
        }
    }

    internal fun sampleFloat(rng: Rng): Float {
        fun linearMap(x: Float): Float = x * scaleVal.toFloat() + locationVal.toFloat()
        val u1 = StandardNormal.sampleFloat(rng)
        val s = shapeVal.toFloat()
        return if (s == 0.0f) {
            linearMap(u1)
        } else {
            val u2 = StandardNormal.sampleFloat(rng)
            val u = max(u1, u2)
            val v = min(u1, u2)
            when (s) {
                -1.0f -> {
                    linearMap(v)
                }

                1.0f -> {
                    linearMap(u)
                }

                else -> {
                    val normalized =
                        ((1.0f + s) * u + (1.0f - s) * v) /
                            (kotlin.math.sqrt(1.0f + s * s) * SQRT_2.toFloat())
                    linearMap(normalized)
                }
            }
        }
    }
}
