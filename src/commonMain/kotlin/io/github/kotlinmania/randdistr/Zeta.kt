// port-lint: source src/zeta.rs
package io.github.kotlinmania.randdistr

import io.github.kotlinmania.rand.Rng
import io.github.kotlinmania.rand.distr.Distribution
import kotlin.math.floor
import kotlin.math.pow

/**
 * Error type returned from [Zeta.new].
 */
public enum class ZetaError {
    /** `s <= 1` or `nan`. */
    STooSmall,
    ;

    override fun toString(): String =
        when (this) {
            STooSmall -> "s <= 1 or is NaN in Zeta distribution"
        }
}

/**
 * The Zeta distribution `Zeta(s)`.
 */
public data class Zeta internal constructor(
    private val sMinus1: Double,
    private val b: Double,
    public val error: ZetaError? = null,
) : Distribution<Double> {
    public companion object {
        /**
         * Construct a new `Zeta` distribution with given `s` parameter.
         */
        public fun new(s: Double): Zeta {
            if (!(s > 1.0)) {
                return Zeta(0.0, 0.0, ZetaError.STooSmall)
            }
            val sMinus1 = s - 1.0
            val two = 2.0
            return Zeta(sMinus1, two.pow(sMinus1), null)
        }
    }

    public fun isOk(): Boolean = error == null

    public fun isErr(): Boolean = error != null

    public fun unwrap(): Zeta {
        if (error != null) {
            throw IllegalStateException(error.toString())
        }
        return this
    }

    public fun unwrapErr(): ZetaError = error ?: throw IllegalStateException("Called unwrapErr on an Ok value")

    override fun sample(rng: Rng): Double {
        while (true) {
            val u = rng.sampleOpenClosed01()
            val x = floor(u.pow(-1.0 / sMinus1))
            if (x.isInfinite()) {
                return x
            }
            val t = (1.0 + 1.0 / x).pow(sMinus1)
            val v = rng.randomDouble()
            if (v * x * (t - 1.0) * b <= t * (b - 1.0)) {
                return x
            }
        }
    }

    internal fun sampleFloat(rng: Rng): Float {
        val sm1 = sMinus1.toFloat()
        val bf = (2.0f).pow(sm1)
        while (true) {
            val u = rng.sampleOpenClosed01Float()
            val x = floor(u.pow(-1.0f / sm1))
            if (x.isInfinite()) {
                return x
            }
            val t = (1.0f + 1.0f / x).pow(sm1)
            val v = rng.randomFloat()
            if (v * x * (t - 1.0f) * bf <= t * (bf - 1.0f)) {
                return x
            }
        }
    }
}
