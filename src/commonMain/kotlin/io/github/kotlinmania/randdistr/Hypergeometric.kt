// port-lint: source src/hypergeometric.rs
package io.github.kotlinmania.randdistr

import io.github.kotlinmania.rand.Rng
import io.github.kotlinmania.rand.distr.Distribution
import io.github.kotlinmania.rand.distr.Uniform
import kotlin.math.exp
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

internal sealed class HypergeometricSamplingMethod {
    internal data class InverseTransform(
        val initialP: Double,
        val initialX: Long,
    ) : HypergeometricSamplingMethod()

    internal data class RejectionAcceptance(
        val m: Double,
        val a: Double,
        val lambdaL: Double,
        val lambdaR: Double,
        val xL: Double,
        val xR: Double,
        val p1: Double,
        val p2: Double,
        val p3: Double,
    ) : HypergeometricSamplingMethod()
}

/**
 * Error type returned from [Hypergeometric.new].
 */
public enum class HypergeometricError {
    /** `totalPopulationSize` is too large, causing floating point underflow. */
    PopulationTooLarge,

    /** `populationWithFeature > totalPopulationSize`. */
    ProbabilityTooLarge,

    /** `sampleSize > totalPopulationSize`. */
    SampleSizeTooLarge,
    ;

    override fun toString(): String =
        when (this) {
            PopulationTooLarge -> {
                "total_population_size is too large causing underflow in geometric distribution"
            }

            ProbabilityTooLarge -> {
                "population_with_feature > total_population_size in geometric distribution"
            }

            SampleSizeTooLarge -> {
                "sample_size > total_population_size in geometric distribution"
            }
        }
}

private fun fractionOfProductsOfFactorials(
    numerator0: ULong,
    numerator1: ULong,
    denominator0: ULong,
    denominator1: ULong,
): Double {
    val minTop = min(numerator0, numerator1)
    val minBottom = min(denominator0, denominator1)
    val minAll = min(minTop, minBottom)

    val maxTop = max(numerator0, numerator1)
    val maxBottom = max(denominator0, denominator1)
    val maxAll = max(maxTop, maxBottom)

    var result = 1.0
    var i = minAll + 1uL
    while (i <= maxAll) {
        val iFloat = i.toDouble()
        if (i <= minTop) {
            result *= iFloat
        }
        if (i <= minBottom) {
            result /= iFloat
        }
        if (i <= maxTop) {
            result *= iFloat
        }
        if (i <= maxBottom) {
            result /= iFloat
        }
        i += 1uL
    }
    return result
}

private const val LOGSQRT2PI: Double = 0.91893853320467274178

internal fun lnOfFactorial(v: Double): Double {
    val v3 = v + 3.0
    val lnFac = (v3 + 0.5) * ln(v3) - v3 + LOGSQRT2PI + 1.0 / (12.0 * v3)
    return lnFac - ln((v + 3.0) * (v + 2.0) * (v + 1.0))
}

/**
 * The hypergeometric distribution `Hypergeometric(N, K, n)`.
 */
public data class Hypergeometric internal constructor(
    private val n1: ULong,
    private val n2: ULong,
    private val k: ULong,
    private val offsetX: Long,
    private val signX: Long,
    private val samplingMethod: HypergeometricSamplingMethod,
    public val error: HypergeometricError? = null,
) : Distribution<ULong> {
    public companion object {
        /**
         * Constructs a new `Hypergeometric` with the shape parameters
         * `N = totalPopulationSize`,
         * `K = populationWithFeature`,
         * `n = sampleSize`.
         */
        public fun new(
            totalPopulationSize: ULong,
            populationWithFeature: ULong,
            sampleSize: ULong,
        ): Hypergeometric {
            val dummyMethod = HypergeometricSamplingMethod.InverseTransform(0.0, 0L)
            if (populationWithFeature > totalPopulationSize) {
                return Hypergeometric(0uL, 0uL, 0uL, 0L, 1L, dummyMethod, HypergeometricError.ProbabilityTooLarge)
            }
            if (sampleSize > totalPopulationSize) {
                return Hypergeometric(0uL, 0uL, 0uL, 0L, 1L, dummyMethod, HypergeometricError.SampleSizeTooLarge)
            }

            val n = totalPopulationSize
            var signX = 1L
            var offsetX = 0L
            val populationWithoutFeature = n - populationWithFeature
            val n1: ULong
            val n2: ULong
            if (populationWithFeature > populationWithoutFeature) {
                signX = -1L
                offsetX = sampleSize.toLong()
                n1 = populationWithoutFeature
                n2 = populationWithFeature
            } else {
                n1 = populationWithFeature
                n2 = populationWithoutFeature
            }

            val k =
                if (sampleSize <= n / 2uL) {
                    sampleSize
                } else {
                    offsetX += n1.toLong() * signX
                    signX *= -1L
                    n - sampleSize
                }

            val hinThreshold = 10.0
            val m = floor((k + 1uL).toDouble() * (n1 + 1uL).toDouble() / (n + 2uL).toDouble())
            val samplingMethod =
                if (m - max(0.0, k.toDouble() - n2.toDouble()) < hinThreshold) {
                    val initialP: Double
                    val initialX: Long
                    if (k < n2) {
                        initialP = fractionOfProductsOfFactorials(n2, n - k, n, n2 - k)
                        initialX = 0L
                    } else {
                        initialP = fractionOfProductsOfFactorials(n1, k, n, k - n2)
                        initialX = (k - n2).toLong()
                    }

                    if (initialP <= 0.0 || !initialP.isFinite()) {
                        return Hypergeometric(0uL, 0uL, 0uL, 0L, 1L, dummyMethod, HypergeometricError.PopulationTooLarge)
                    }

                    HypergeometricSamplingMethod.InverseTransform(initialP, initialX)
                } else {
                    val a =
                        lnOfFactorial(m) +
                            lnOfFactorial(n1.toDouble() - m) +
                            lnOfFactorial(k.toDouble() - m) +
                            lnOfFactorial((n2 - k).toDouble() + m)

                    val num = (n - k).toDouble() * k.toDouble() * n1.toDouble() * n2.toDouble()
                    val den = (n - 1uL).toDouble() * n.toDouble() * n.toDouble()
                    val d = 1.5 * sqrt(num / den) + 0.5

                    val xL = m - d + 0.5
                    val xR = m + d + 0.5

                    val kL =
                        exp(
                            a - lnOfFactorial(xL) -
                                lnOfFactorial(n1.toDouble() - xL) -
                                lnOfFactorial(k.toDouble() - xL) -
                                lnOfFactorial((n2 - k).toDouble() + xL),
                        )
                    val kR =
                        exp(
                            a - lnOfFactorial(xR - 1.0) -
                                lnOfFactorial(n1.toDouble() - xR + 1.0) -
                                lnOfFactorial(k.toDouble() - xR + 1.0) -
                                lnOfFactorial((n2 - k).toDouble() + xR - 1.0),
                        )

                    val numL = xL * ((n2 - k).toDouble() + xL)
                    val denL = (n1.toDouble() - xL + 1.0) * (k.toDouble() - xL + 1.0)
                    val lambdaL = -ln(numL / denL)

                    val numR = (n1.toDouble() - xR + 1.0) * (k.toDouble() - xR + 1.0)
                    val denR = xR * ((n2 - k).toDouble() + xR)
                    val lambdaR = -ln(numR / denR)

                    val p1 = 2.0 * d
                    val p2 = p1 + kL / lambdaL
                    val p3 = p2 + kR / lambdaR

                    HypergeometricSamplingMethod.RejectionAcceptance(
                        m = m,
                        a = a,
                        lambdaL = lambdaL,
                        lambdaR = lambdaR,
                        xL = xL,
                        xR = xR,
                        p1 = p1,
                        p2 = p2,
                        p3 = p3,
                    )
                }

            return Hypergeometric(
                n1 = n1,
                n2 = n2,
                k = k,
                offsetX = offsetX,
                signX = signX,
                samplingMethod = samplingMethod,
                error = null,
            )
        }
    }

    public fun isOk(): Boolean = error == null

    public fun isErr(): Boolean = error != null

    public fun unwrap(): Hypergeometric {
        if (error != null) {
            throw IllegalStateException(error.toString())
        }
        return this
    }

    public fun unwrapErr(): HypergeometricError = error ?: throw IllegalStateException("Called unwrapErr on an Ok value")

    override fun sample(rng: Rng): ULong {
        val sampledX: Long =
            when (val method = samplingMethod) {
                is HypergeometricSamplingMethod.InverseTransform -> {
                    var p = method.initialP
                    var x = method.initialX
                    var u = rng.randomDouble()
                    while (u > p && x < k.toLong()) {
                        u -= p
                        p *= ((n1.toLong() - x) * (k.toLong() - x)).toDouble()
                        p /= ((x + 1L) * (n2.toLong() - k.toLong() + 1L + x)).toDouble()
                        x += 1L
                    }
                    x
                }

                is HypergeometricSamplingMethod.RejectionAcceptance -> {
                    val m = method.m
                    val a = method.a
                    val lambdaL = method.lambdaL
                    val lambdaR = method.lambdaR
                    val xL = method.xL
                    val xR = method.xR
                    val p1 = method.p1
                    val p2 = method.p2
                    val p3 = method.p3
                    val distrRegionSelect = Uniform.new(0.0, p3).unwrap()

                    var resultY: Long
                    while (true) {
                        var y: Double
                        var v: Double
                        while (true) {
                            val u = distrRegionSelect.sample(rng)
                            v = rng.randomDouble()
                            if (u <= p1) {
                                y = floor(xL + u)
                                break
                            } else if (u <= p2) {
                                y = floor(xL + ln(v) / lambdaL)
                                if (y.toLong() >= max(0L, k.toLong() - n2.toLong())) {
                                    v = v * (u - p1) * lambdaL
                                    break
                                }
                            } else {
                                y = floor(xR - ln(v) / lambdaR)
                                if (y >= 0.0 && y.toULong() <= min(n1, k)) {
                                    v = v * (u - p2) * lambdaR
                                    break
                                }
                            }
                        }

                        if (m < 100.0 || y <= 50.0) {
                            var f = 1.0
                            if (m < y) {
                                var i = m.toULong() + 1uL
                                val yU64 = y.toULong()
                                while (i <= yU64) {
                                    f *= (n1 - i + 1uL).toDouble() * (k - i + 1uL).toDouble()
                                    f /= i.toDouble() * (n2 - k + i).toDouble()
                                    i += 1uL
                                }
                            } else {
                                var i = y.toULong() + 1uL
                                val mU64 = m.toULong()
                                while (i <= mU64) {
                                    f *= i.toDouble() * (n2 - k + i).toDouble()
                                    f /= (n1 - i + 1uL).toDouble() * (k - i + 1uL).toDouble()
                                    i += 1uL
                                }
                            }
                            if (v <= f) {
                                resultY = y.toLong()
                                break
                            }
                        } else {
                            val y1 = y + 1.0
                            val ym = y - m
                            val yn = n1.toDouble() - y + 1.0
                            val yk = k.toDouble() - y + 1.0
                            val nk = n2.toDouble() - k.toDouble() + y1
                            val r = -ym / y1
                            val s = ym / yn
                            val t = ym / yk
                            val e = -ym / nk
                            val g = yn * yk / (y1 * nk) - 1.0
                            val dg = if (g < 0.0) 1.0 + g else 1.0
                            val gu = g * (1.0 + g * (-0.5 + g / 3.0))
                            val gl = gu - g.pow(4) / (4.0 * dg)
                            val xm = m + 0.5
                            val xn = n1.toDouble() - m + 0.5
                            val xk = k.toDouble() - m + 0.5
                            val nm = n2.toDouble() - k.toDouble() + xm
                            val ub =
                                xm * r * (1.0 + r * (-0.5 + r / 3.0)) +
                                    xn * s * (1.0 + s * (-0.5 + s / 3.0)) +
                                    xk * t * (1.0 + t * (-0.5 + t / 3.0)) +
                                    nm * e * (1.0 + e * (-0.5 + e / 3.0)) +
                                    y * gu -
                                    m * gl +
                                    0.0034
                            val av = ln(v)
                            if (av > ub) {
                                continue
                            }
                            val dr = if (r < 0.0) xm * r.pow(4) / (1.0 + r) else xm * r.pow(4)
                            val ds = if (s < 0.0) xn * s.pow(4) / (1.0 + s) else xn * s.pow(4)
                            val dt = if (t < 0.0) xk * t.pow(4) / (1.0 + t) else xk * t.pow(4)
                            val de = if (e < 0.0) nm * e.pow(4) / (1.0 + e) else nm * e.pow(4)

                            if (av < ub - 0.25 * (dr + ds + dt + de) + (y + m) * (gl - gu) - 0.0078) {
                                resultY = y.toLong()
                                break
                            }

                            val avCritical =
                                a -
                                    lnOfFactorial(y) -
                                    lnOfFactorial(n1.toDouble() - y) -
                                    lnOfFactorial(k.toDouble() - y) -
                                    lnOfFactorial((n2 - k).toDouble() + y)
                            if (ln(v) <= avCritical) {
                                resultY = y.toLong()
                                break
                            }
                        }
                    }
                    resultY
                }
            }
        return (offsetX + signX * sampledX).toULong()
    }
}
