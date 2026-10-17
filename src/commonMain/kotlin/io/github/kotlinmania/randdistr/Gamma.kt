// port-lint: source src/gamma.rs
package io.github.kotlinmania.randdistr

import io.github.kotlinmania.rand.Rng
import io.github.kotlinmania.rand.distr.Distribution
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Error type returned from [Gamma.new].
 */
public enum class GammaError {
    /** `shape <= 0` or `nan`. */
    ShapeTooSmall,

    /** `scale <= 0` or `nan`. */
    ScaleTooSmall,

    /** `1 / scale == 0`. */
    ScaleTooLarge,
    ;

    override fun toString(): String =
        when (this) {
            ShapeTooSmall -> "shape is not positive in gamma distribution"
            ScaleTooSmall -> "scale is not positive in gamma distribution"
            ScaleTooLarge -> "scale is infinity in gamma distribution"
        }
}

internal sealed class GammaRepr {
    internal data class Large(
        val large: GammaLarge,
    ) : GammaRepr()

    internal data object One : GammaRepr()

    internal data class Small(
        val small: GammaSmall,
    ) : GammaRepr()
}

internal data class GammaLarge(
    val scale: Double,
    val c: Double,
    val d: Double,
) {
    companion object {
        fun new(
            shape: Double,
            scale: Double,
        ): GammaLarge {
            val d = shape - 1.0 / 3.0
            val c = 1.0 / sqrt(9.0 * d)
            return GammaLarge(scale, c, d)
        }
    }

    fun sample(rng: Rng): Double {
        while (true) {
            val x = rng.sample(StandardNormal)
            val vcbrt = 1.0 + c * x
            if (vcbrt <= 0.0) {
                continue
            }
            val v = vcbrt * vcbrt * vcbrt
            val u = rng.sampleOpen01()
            val xSqr = x * x
            if (u < 1.0 - 0.0331 * xSqr * xSqr ||
                ln(u) < 0.5 * xSqr + d * (1.0 - v + ln(v))
            ) {
                return d * v * scale
            }
        }
    }
}

internal data class GammaSmall(
    val invAlpha: Double,
    val large: GammaLarge,
) {
    companion object {
        fun new(
            shape: Double,
            scale: Double,
        ): GammaSmall {
            val invAlpha = 1.0 / shape
            return GammaSmall(invAlpha, GammaLarge.new(shape + 1.0, scale))
        }
    }

    fun sample(rng: Rng): Double {
        val u = rng.sampleOpen01()
        return large.sample(rng) * u.pow(invAlpha)
    }
}

/**
 * The Gamma distribution `Gamma(shape, scale)` distribution.
 */
public data class Gamma internal constructor(
    private val repr: GammaRepr,
    private val scale: Double,
    public val error: GammaError? = null,
) : Distribution<Double> {
    public companion object {
        /**
         * Construct an object representing the `Gamma(shape, scale)`
         * distribution.
         */
        public fun new(
            shape: Double,
            scale: Double,
        ): Gamma {
            if (!(shape > 0.0)) {
                return Gamma(GammaRepr.One, 0.0, GammaError.ShapeTooSmall)
            }
            if (!(scale > 0.0)) {
                return Gamma(GammaRepr.One, 0.0, GammaError.ScaleTooSmall)
            }
            val repr =
                when {
                    shape == 1.0 -> {
                        if (1.0 / scale == 0.0) {
                            return Gamma(GammaRepr.One, 0.0, GammaError.ScaleTooLarge)
                        }
                        GammaRepr.One
                    }

                    shape < 1.0 -> {
                        GammaRepr.Small(GammaSmall.new(shape, scale))
                    }

                    else -> {
                        GammaRepr.Large(GammaLarge.new(shape, scale))
                    }
                }
            return Gamma(repr, scale, null)
        }
    }

    public fun isOk(): Boolean = error == null

    public fun isErr(): Boolean = error != null

    public fun unwrap(): Gamma {
        if (error != null) {
            throw IllegalStateException(error.toString())
        }
        return this
    }

    public fun unwrapErr(): GammaError = error ?: throw IllegalStateException("Called unwrapErr on an Ok value")

    override fun sample(rng: Rng): Double =
        when (val r = repr) {
            is GammaRepr.Small -> r.small.sample(rng)
            is GammaRepr.One -> rng.sample(Exp1) * scale
            is GammaRepr.Large -> r.large.sample(rng)
        }
}
