// port-lint: source src/exponential.rs
package io.github.kotlinmania.randdistr

import io.github.kotlinmania.rand.Rng
import io.github.kotlinmania.rand.distr.Distribution
import kotlin.math.exp
import kotlin.math.ln

/**
 * The standard exponential distribution `Exp(1)`.
 */
public object Exp1 : Distribution<Double> {
    private fun pdf(x: Double): Double = exp(-x)

    private fun zeroCase(
        rng: Rng,
        u: Double,
    ): Double {
        val ignored = u
        if (ignored.isNaN()) return Double.NaN
        return ZIG_EXP_R - ln(rng.sampleOpen01())
    }

    override fun sample(rng: Rng): Double =
        ziggurat(
            rng,
            false,
            ZIG_EXP_X,
            ZIG_EXP_F,
            ::pdf,
            ::zeroCase,
        )

    internal fun sampleFloat(rng: Rng): Float = sample(rng).toFloat()
}

/**
 * Error type returned from [Exp.new].
 */
public enum class ExpError {
    /** `lambda < 0` or `nan`. */
    LambdaTooSmall,
    ;

    override fun toString(): String =
        when (this) {
            LambdaTooSmall -> "lambda < 0 or is NaN in exponential distribution"
        }
}

/**
 * The exponential distribution `Exp(lambda)`.
 */
public data class Exp internal constructor(
    internal val lambdaInverse: Double,
    public val error: ExpError? = null,
) : Distribution<Double> {
    public companion object {
        /**
         * Construct a new `Exp` with the given shape parameter `lambda`.
         */
        public fun new(lambda: Double): Exp {
            if (!(lambda >= 0.0)) {
                return Exp(0.0, ExpError.LambdaTooSmall)
            }
            return Exp(1.0 / lambda, null)
        }
    }

    public fun isOk(): Boolean = error == null

    public fun isErr(): Boolean = error != null

    public fun unwrap(): Exp {
        if (error != null) {
            throw IllegalStateException(error.toString())
        }
        return this
    }

    public fun unwrapErr(): ExpError = error ?: throw IllegalStateException("Called unwrapErr on an Ok value")

    override fun sample(rng: Rng): Double = rng.sample(Exp1) * lambdaInverse

    internal fun sampleFloat(rng: Rng): Float = Exp1.sampleFloat(rng) * lambdaInverse.toFloat()
}
