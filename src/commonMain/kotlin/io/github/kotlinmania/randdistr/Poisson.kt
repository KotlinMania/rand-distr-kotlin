// port-lint: source src/poisson.rs
package io.github.kotlinmania.randdistr

import io.github.kotlinmania.rand.Rng
import io.github.kotlinmania.rand.distr.Distribution
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Error type returned from [Poisson.new].
 */
public enum class PoissonError {
    /** `lambda <= 0` */
    ShapeTooSmall,

    /** `lambda = infinity` or `lambda = nan` */
    NonFinite,

    /** `lambda` is too large, see [Poisson.MAX_LAMBDA] */
    ShapeTooLarge,
    ;

    override fun toString(): String =
        when (this) {
            ShapeTooSmall -> "lambda is not positive in Poisson distribution"
            NonFinite -> "lambda is infinite or nan in Poisson distribution"
            ShapeTooLarge -> "lambda is too large in Poisson distribution, see Poisson::MAX_LAMBDA"
        }
}

internal data class KnuthMethod(
    val expLambda: Double,
) : Distribution<Double> {
    companion object {
        fun new(lambda: Double): KnuthMethod = KnuthMethod(exp(-lambda))
    }

    override fun sample(rng: Rng): Double {
        var result = 1.0
        var p = rng.randomDouble()
        while (p > expLambda) {
            p *= rng.randomDouble()
            result += 1.0
        }
        return result - 1.0
    }
}

internal data class RejectionMethod(
    val lambda: Double,
    val s: Double,
    val d: Double,
    val l: Double,
    val c: Double,
    val c0: Double,
    val c1: Double,
    val c2: Double,
    val c3: Double,
    val omega: Double,
) : Distribution<Double> {
    companion object {
        private val FACT: DoubleArray =
            doubleArrayOf(
                1.0,
                1.0,
                2.0,
                6.0,
                24.0,
                120.0,
                720.0,
                5040.0,
                40320.0,
                362880.0,
            )

        private val A: DoubleArray =
            doubleArrayOf(
                -0.5000000002,
                0.3333333343,
                -0.2499998565,
                0.1999997049,
                -0.1666848753,
                0.1428833286,
                -0.1241963125,
                0.1101687109,
                -0.1142650302,
                0.1055093006,
            )

        fun new(lambda: Double): RejectionMethod {
            val b1 = (1.0 / 24.0) / lambda
            val b2 = 0.3 * b1 * b1
            val c3 = (1.0 / 7.0) * b1 * b2
            val c2 = b2 - 15.0 * c3
            val c1 = b1 - 6.0 * b2 + 45.0 * c3
            val c0 = 1.0 - b1 + 3.0 * b2 - 15.0 * c3
            val s = sqrt(lambda)
            return RejectionMethod(
                lambda = lambda,
                s = s,
                d = 6.0 * lambda * lambda,
                l = floor(lambda - 1.1484),
                c = 0.1069 / lambda,
                c0 = c0,
                c1 = c1,
                c2 = c2,
                c3 = c3,
                omega = 1.0 / sqrt(2.0 * PI) / s,
            )
        }
    }

    private fun evalF(k: Double): DoubleArray {
        val px: Double
        val py: Double
        if (k < 10.0) {
            px = -lambda
            py = lambda.pow(k) / FACT[k.toInt()]
        } else {
            var delta = 1.0 / (12.0 * k)
            delta -= 4.8 * delta * delta * delta
            val v = (lambda - k) / k
            px =
                if (abs(v) <= 0.25) {
                    var poly = 0.0
                    for (idx in A.indices.reversed()) {
                        poly = poly * v + A[idx]
                    }
                    k * v * v * poly - delta
                } else {
                    k * ln(1.0 + v) - (lambda - k) - delta
                }
            py = 1.0 / sqrt(2.0 * PI) / sqrt(k)
        }
        val x = (k - lambda + 0.5) / s
        val x2 = x * x
        val fx = -0.5 * x2
        val fy = omega * (((c3 * x2 + c2) * x2 + c1) * x2 + c0)
        return doubleArrayOf(px, py, fx, fy)
    }

    override fun sample(rng: Rng): Double {
        val normal = Normal.new(lambda, s).unwrap()
        val g = normal.sample(rng)
        if (g >= 0.0) {
            val k1 = floor(g)
            if (k1 >= l) {
                return k1
            }
            val u = rng.randomDouble()
            val diff = lambda - k1
            if (d * u >= diff * diff * diff) {
                return k1
            }
            val fVals = evalF(k1)
            val px = fVals[0]
            val py = fVals[1]
            val fx = fVals[2]
            val fy = fVals[3]
            if (fy * (1.0 - u) <= py * exp(px - fx)) {
                return k1
            }
        }

        while (true) {
            val e = Exp1.sample(rng)
            val u = rng.randomDouble() * 2.0 - 1.0
            val signU = if (u >= 0.0) 1.0 else -1.0
            val t = 1.8 + e * signU
            if (t > -0.6744) {
                val k2 = floor(lambda + s * t)
                val fVals = evalF(k2)
                val px = fVals[0]
                val py = fVals[1]
                val fx = fVals[2]
                val fy = fVals[3]
                if (c * abs(u) <= py * exp(px + e) - fy * exp(fx + e)) {
                    return k2
                }
            }
        }
    }
}

internal sealed class PoissonMethod {
    internal data class Knuth(
        val method: KnuthMethod,
    ) : PoissonMethod()

    internal data class Rejection(
        val method: RejectionMethod,
    ) : PoissonMethod()
}

/**
 * The Poisson distribution `Poisson(lambda)`.
 */
public data class Poisson internal constructor(
    private val method: PoissonMethod,
    public val error: PoissonError? = null,
) : Distribution<Double> {
    public companion object {
        /**
         * The maximum supported value of `lambda`.
         */
        public const val MAX_LAMBDA: Double = 1.844e19

        /**
         * Construct a new `Poisson` with the given shape parameter
         * `lambda`.
         */
        public fun new(lambda: Double): Poisson {
            val dummy = PoissonMethod.Knuth(KnuthMethod(0.0))
            if (!lambda.isFinite()) {
                return Poisson(dummy, PoissonError.NonFinite)
            }
            if (!(lambda > 0.0)) {
                return Poisson(dummy, PoissonError.ShapeTooSmall)
            }
            val method =
                if (lambda < 12.0) {
                    PoissonMethod.Knuth(KnuthMethod.new(lambda))
                } else {
                    if (lambda > MAX_LAMBDA) {
                        return Poisson(dummy, PoissonError.ShapeTooLarge)
                    }
                    PoissonMethod.Rejection(RejectionMethod.new(lambda))
                }
            return Poisson(method, null)
        }
    }

    public fun isOk(): Boolean = error == null

    public fun isErr(): Boolean = error != null

    public fun unwrap(): Poisson {
        if (error != null) {
            throw IllegalStateException(error.toString())
        }
        return this
    }

    public fun unwrapErr(): PoissonError = error ?: throw IllegalStateException("Called unwrapErr on an Ok value")

    override fun sample(rng: Rng): Double =
        when (val m = method) {
            is PoissonMethod.Knuth -> m.method.sample(rng)
            is PoissonMethod.Rejection -> m.method.sample(rng)
        }
}
