// port-lint: source src/normal.rs
package io.github.kotlinmania.randdistr

import io.github.kotlinmania.rand.Rng
import io.github.kotlinmania.rand.distr.Distribution
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.sqrt

/**
 * The standard normal distribution `N(0, 1)`.
 */
public object StandardNormal : Distribution<Double> {
    private fun pdf(x: Double): Double = exp(-x * x / 2.0)

    private fun zeroCase(
        rng: Rng,
        u: Double,
    ): Double {
        var x = 1.0
        var y = 0.0
        while (-2.0 * y < x * x) {
            val xPrime = rng.sampleOpen01()
            val yPrime = rng.sampleOpen01()
            x = ln(xPrime) / ZIG_NORM_R
            y = ln(yPrime)
        }
        return if (u < 0.0) x - ZIG_NORM_R else ZIG_NORM_R - x
    }

    override fun sample(rng: Rng): Double =
        ziggurat(
            rng,
            true,
            ZIG_NORM_X,
            ZIG_NORM_F,
            ::pdf,
            ::zeroCase,
        )

    internal fun sampleFloat(rng: Rng): Float = sample(rng).toFloat()
}

/**
 * Error type returned from [Normal.new] and [LogNormal.new].
 */
public enum class NormalError {
    /** The mean value is too small (log-normal samples must be positive) */
    MeanTooSmall,

    /** The standard deviation or other dispersion parameter is not finite or is negative. */
    BadVariance,
    ;

    override fun toString(): String =
        when (this) {
            MeanTooSmall -> "mean < 0 or is NaN in log-normal distribution"
            BadVariance -> "variation parameter is non-finite or < 0 in (log)normal distribution"
        }
}

/**
 * The normal distribution `N(mean, stdDev**2)`.
 */
public data class Normal internal constructor(
    private val meanVal: Double,
    private val stdDevVal: Double,
    public val error: NormalError? = null,
) : Distribution<Double> {
    public companion object {
        /**
         * Construct, from mean and standard deviation.
         */
        public fun new(
            mean: Double,
            stdDev: Double,
        ): Normal {
            if (!stdDev.isFinite()) {
                return Normal(mean, stdDev, NormalError.BadVariance)
            }
            return Normal(mean, stdDev, null)
        }

        /**
         * Construct, from mean and coefficient of variation.
         */
        public fun fromMeanCv(
            mean: Double,
            cv: Double,
        ): Normal {
            if (!cv.isFinite() || cv < 0.0) {
                return Normal(mean, cv, NormalError.BadVariance)
            }
            val stdDev = cv * mean
            return Normal(mean, stdDev, null)
        }
    }

    /**
     * Returns the mean (`μ`) of the distribution.
     */
    public fun mean(): Double = meanVal

    /**
     * Returns the standard deviation (`σ`) of the distribution.
     */
    public fun stdDev(): Double = stdDevVal

    /**
     * Sample from a z-score.
     */
    public fun fromZscore(zscore: Double): Double = meanVal + stdDevVal * zscore

    public fun isOk(): Boolean = error == null

    public fun isErr(): Boolean = error != null

    public fun unwrap(): Normal {
        if (error != null) {
            throw IllegalStateException(error.toString())
        }
        return this
    }

    public fun unwrapErr(): NormalError = error ?: throw IllegalStateException("Called unwrapErr on an Ok value")

    override fun sample(rng: Rng): Double = fromZscore(rng.sample(StandardNormal))

    internal fun sampleFloat(rng: Rng): Float = meanVal.toFloat() + stdDevVal.toFloat() * StandardNormal.sampleFloat(rng)
}

/**
 * The log-normal distribution `ln N(mean, stdDev**2)`.
 */
public data class LogNormal internal constructor(
    internal val norm: Normal,
    public val error: NormalError? = null,
) : Distribution<Double> {
    public companion object {
        /**
         * Construct, from (log-space) mean and standard deviation.
         */
        public fun new(
            mean: Double,
            stdDev: Double,
        ): LogNormal {
            val norm = Normal.new(mean, stdDev)
            if (norm.error != null) {
                return LogNormal(norm, norm.error)
            }
            return LogNormal(norm, null)
        }

        /**
         * Construct, from (linear-space) mean and coefficient of variation.
         */
        public fun fromMeanCv(
            mean: Double,
            cv: Double,
        ): LogNormal {
            if (cv == 0.0) {
                val mu = ln(mean)
                val norm = Normal.new(mu, 0.0).unwrap()
                return LogNormal(norm, null)
            }
            if (!(mean > 0.0)) {
                return LogNormal(Normal(0.0, 0.0, NormalError.MeanTooSmall), NormalError.MeanTooSmall)
            }
            if (!(cv >= 0.0)) {
                return LogNormal(Normal(0.0, 0.0, NormalError.BadVariance), NormalError.BadVariance)
            }
            val a = 1.0 + cv * cv
            val mu = 0.5 * ln(mean * mean / a)
            val sigma = sqrt(ln(a))
            return new(mu, sigma)
        }
    }

    /**
     * Sample from a z-score.
     */
    public fun fromZscore(zscore: Double): Double = exp(norm.fromZscore(zscore))

    public fun isOk(): Boolean = error == null

    public fun isErr(): Boolean = error != null

    public fun unwrap(): LogNormal {
        if (error != null) {
            throw IllegalStateException(error.toString())
        }
        return this
    }

    public fun unwrapErr(): NormalError = error ?: throw IllegalStateException("Called unwrapErr on an Ok value")

    override fun sample(rng: Rng): Double = exp(norm.sample(rng))

    internal fun sampleFloat(rng: Rng): Float = kotlin.math.exp(norm.sampleFloat(rng))
}
