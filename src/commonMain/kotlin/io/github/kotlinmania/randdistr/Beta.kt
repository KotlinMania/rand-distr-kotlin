// port-lint: source src/beta.rs
package io.github.kotlinmania.randdistr

import io.github.kotlinmania.rand.Rng
import io.github.kotlinmania.rand.distr.Distribution
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.sqrt

internal sealed class BetaAlgorithm {
    internal data class BB(
        val bb: BetaBB,
    ) : BetaAlgorithm()

    internal data class BC(
        val bc: BetaBC,
    ) : BetaAlgorithm()
}

internal data class BetaBB(
    val alpha: Double,
    val beta: Double,
    val gamma: Double,
)

internal data class BetaBC(
    val alpha: Double,
    val beta: Double,
    val kappa1: Double,
    val kappa2: Double,
)

/**
 * Error type returned from [Beta.new].
 */
public enum class BetaError {
    /** `alpha <= 0` or `nan`. */
    AlphaTooSmall,

    /** `beta <= 0` or `nan`. */
    BetaTooSmall,
    ;

    override fun toString(): String =
        when (this) {
            AlphaTooSmall -> "alpha is not positive in beta distribution"
            BetaTooSmall -> "beta is not positive in beta distribution"
        }
}

/**
 * The Beta distribution `Beta(alpha, beta)`.
 */
public data class Beta internal constructor(
    private val a: Double,
    private val b: Double,
    private val switchedParams: Boolean,
    private val algorithm: BetaAlgorithm,
    public val error: BetaError? = null,
) : Distribution<Double> {
    public companion object {
        /**
         * Construct an object representing the `Beta(alpha, beta)`
         * distribution.
         */
        public fun new(
            alpha: Double,
            beta: Double,
        ): Beta {
            if (!(alpha > 0.0)) {
                return Beta(0.0, 0.0, false, BetaAlgorithm.BB(BetaBB(0.0, 0.0, 0.0)), BetaError.AlphaTooSmall)
            }
            if (!(beta > 0.0)) {
                return Beta(0.0, 0.0, false, BetaAlgorithm.BB(BetaBB(0.0, 0.0, 0.0)), BetaError.BetaTooSmall)
            }
            val a0 = alpha
            val b0 = beta
            val minA: Double
            val maxB: Double
            val initialSwitched: Boolean
            if (a0 < b0) {
                minA = a0
                maxB = b0
                initialSwitched = false
            } else {
                minA = b0
                maxB = a0
                initialSwitched = true
            }
            if (minA > 1.0) {
                val sumAlpha = minA + maxB
                val two = 2.0
                val betaNumer = sumAlpha - two
                val betaDenom = two * minA * maxB - sumAlpha
                val betaParam = sqrt(betaNumer / betaDenom)
                val gamma = minA + 1.0 / betaParam
                return Beta(
                    a = minA,
                    b = maxB,
                    switchedParams = initialSwitched,
                    algorithm = BetaAlgorithm.BB(BetaBB(sumAlpha, betaParam, gamma)),
                    error = null,
                )
            } else {
                val bcA = maxB
                val bcB = minA
                val bcSwitched = !initialSwitched
                val sumAlpha = bcA + bcB
                val betaParam = 1.0 / bcB
                val delta = 1.0 + bcA - bcB
                val kappa1 =
                    delta * ((1.0 / 18.0 / 4.0) + (3.0 / 18.0 / 4.0) * bcB) /
                        (bcA * betaParam - (14.0 / 18.0))
                val kappa2 = 0.25 + (0.5 + 0.25 / delta) * bcB
                return Beta(
                    a = bcA,
                    b = bcB,
                    switchedParams = bcSwitched,
                    algorithm = BetaAlgorithm.BC(BetaBC(sumAlpha, betaParam, kappa1, kappa2)),
                    error = null,
                )
            }
        }
    }

    public fun isOk(): Boolean = error == null

    public fun isErr(): Boolean = error != null

    public fun unwrap(): Beta {
        if (error != null) {
            throw IllegalStateException(error.toString())
        }
        return this
    }

    public fun unwrapErr(): BetaError = error ?: throw IllegalStateException("Called unwrapErr on an Ok value")

    override fun sample(rng: Rng): Double {
        var w: Double
        when (val algoWrap = algorithm) {
            is BetaAlgorithm.BB -> {
                val algo = algoWrap.bb
                while (true) {
                    val u1 = rng.sampleOpen01()
                    val u2 = rng.sampleOpen01()
                    val v = algo.beta * ln(u1 / (1.0 - u1))
                    w = a * exp(v)
                    val z = u1 * u1 * u2
                    val r = algo.gamma * v - ln(4.0)
                    val s = a + r - w
                    if (s + 1.0 + ln(5.0) >= 5.0 * z) {
                        break
                    }
                    val t = ln(z)
                    if (s >= t) {
                        break
                    }
                    if (!(r + algo.alpha * ln(algo.alpha / (b + w)) < t)) {
                        break
                    }
                }
            }

            is BetaAlgorithm.BC -> {
                val algo = algoWrap.bc
                while (true) {
                    val z: Double
                    val u1 = rng.sampleOpen01()
                    val u2 = rng.sampleOpen01()
                    if (u1 < 0.5) {
                        val y = u1 * u2
                        z = u1 * y
                        if (0.25 * u2 + z - y >= algo.kappa1) {
                            continue
                        }
                    } else {
                        z = u1 * u1 * u2
                        if (z <= 0.25) {
                            val v = algo.beta * ln(u1 / (1.0 - u1))
                            w = a * exp(v)
                            break
                        }
                        if (z >= algo.kappa2) {
                            continue
                        }
                    }
                    val v = algo.beta * ln(u1 / (1.0 - u1))
                    w = a * exp(v)
                    if (!(algo.alpha * (ln(algo.alpha / (b + w)) + v) - ln(4.0) < ln(z))) {
                        break
                    }
                }
            }
        }
        return if (!switchedParams) {
            if (w == Double.POSITIVE_INFINITY) {
                return 1.0
            }
            w / (b + w)
        } else {
            if (w == Double.POSITIVE_INFINITY) {
                return 0.0
            }
            b / (b + w)
        }
    }
}
