// port-lint: source src/pareto.rs
package io.github.kotlinmania.randdistr

import io.github.kotlinmania.rand.Rng
import io.github.kotlinmania.rand.distr.Distribution
import kotlin.math.pow

/**
 * Error type returned from [Pareto.new].
 */
public enum class ParetoError {
    /** `scale <= 0` or `nan`. */
    ScaleTooSmall,

    /** `shape <= 0` or `nan`. */
    ShapeTooSmall,
    ;

    override fun toString(): String =
        when (this) {
            ScaleTooSmall -> "scale is not positive in Pareto distribution"
            ShapeTooSmall -> "shape is not positive in Pareto distribution"
        }
}

/**
 * The Pareto distribution `Pareto(scale, shape)`.
 */
public data class Pareto internal constructor(
    private val scale: Double,
    private val invNegShape: Double,
    public val error: ParetoError? = null,
) : Distribution<Double> {
    public companion object {
        /**
         * Construct a new Pareto distribution with given `scale` and `shape`.
         */
        public fun new(
            scale: Double,
            shape: Double,
        ): Pareto {
            if (!(scale > 0.0)) {
                return Pareto(0.0, 0.0, ParetoError.ScaleTooSmall)
            }
            if (!(shape > 0.0)) {
                return Pareto(0.0, 0.0, ParetoError.ShapeTooSmall)
            }
            return Pareto(scale, -1.0 / shape, null)
        }
    }

    public fun isOk(): Boolean = error == null

    public fun isErr(): Boolean = error != null

    public fun unwrap(): Pareto {
        if (error != null) {
            throw IllegalStateException(error.toString())
        }
        return this
    }

    public fun unwrapErr(): ParetoError = error ?: throw IllegalStateException("Called unwrapErr on an Ok value")

    override fun sample(rng: Rng): Double {
        val u = rng.sampleOpenClosed01()
        return scale * u.pow(invNegShape)
    }

    internal fun sampleFloat(rng: Rng): Float {
        val u = rng.sampleOpenClosed01Float()
        return scale.toFloat() * u.pow(invNegShape.toFloat())
    }
}
