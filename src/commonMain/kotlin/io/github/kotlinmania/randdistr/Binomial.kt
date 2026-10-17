// port-lint: source src/binomial.rs
package io.github.kotlinmania.randdistr

import io.github.kotlinmania.rand.Rng
import io.github.kotlinmania.rand.distr.Distribution
import io.github.kotlinmania.rand.distr.Uniform
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sqrt

internal sealed class BinomialMethod {
    internal data class Binv(
        val binv: BinomialBinv,
        val flipped: Boolean,
    ) : BinomialMethod()

    internal data class Btpe(
        val btpe: BinomialBtpe,
        val flipped: Boolean,
    ) : BinomialMethod()

    internal data class Poisson(
        val knuth: KnuthMethod,
    ) : BinomialMethod()

    internal data class Constant(
        val value: ULong,
    ) : BinomialMethod()
}

internal data class BinomialBinv(
    val r: Double,
    val s: Double,
    val a: Double,
    val n: ULong,
)

internal data class BinomialBtpe(
    val n: ULong,
    val p: Double,
    val m: Long,
    val p1: Double,
)

/**
 * Error type returned from [Binomial.new].
 */
public enum class BinomialError {
    /** `p < 0` or `nan`. */
    ProbabilityTooSmall,

    /** `p > 1`. */
    ProbabilityTooLarge,
    ;

    override fun toString(): String =
        when (this) {
            ProbabilityTooSmall -> "p < 0 or is NaN in binomial distribution"
            ProbabilityTooLarge -> "p > 1 in binomial distribution"
        }
}

private fun f64ToI64(x: Double): Long {
    check(x < Long.MAX_VALUE.toDouble())
    return x.toLong()
}

private fun sampleBinv(
    binv: BinomialBinv,
    flipped: Boolean,
    rng: Rng,
): ULong {
    val binvMaxX = 110uL
    var sample: ULong
    outer@ while (true) {
        var r = binv.r
        var u = rng.randomDouble()
        var x = 0uL
        while (u > r) {
            u -= r
            x += 1uL
            if (x > binvMaxX) {
                continue@outer
            }
            r *= binv.a / x.toDouble() - binv.s
        }
        sample = x
        break
    }
    return if (flipped) {
        binv.n - sample
    } else {
        sample
    }
}

private fun btpeLambda(a: Double): Double = a * (1.0 + 0.5 * a)

private fun btpeStirling(a: Double): Double {
    val a2 = a * a
    return (13860.0 - (462.0 - (132.0 - (99.0 - 140.0 / a2) / a2) / a2) / a2) / a / 166320.0
}

private fun sampleBtpe(
    btpe: BinomialBtpe,
    flipped: Boolean,
    rng: Rng,
): ULong {
    val squeezeThreshold = 20L
    val n = btpe.n.toDouble()
    val np = n * btpe.p
    val q = 1.0 - btpe.p
    val npq = np * q
    val fM = np + btpe.p
    val m = btpe.m
    val p1 = btpe.p1
    val xM = m.toDouble() + 0.5
    val xL = xM - p1
    val xR = xM + p1
    val c = 0.134 + 20.5 / (15.3 + m.toDouble())
    val p2 = p1 * (1.0 + 2.0 * c)
    val lambdaL = btpeLambda((fM - xL) / (fM - xL * btpe.p))
    val lambdaR = btpeLambda((xR - fM) / (xR * q))
    val p3 = p2 + c / lambdaL
    val p4 = p3 + c / lambdaR

    var y: Long
    val genU = Uniform.new(0.0, p4).unwrap()
    val genV = Uniform.new(0.0, 1.0).unwrap()

    while (true) {
        val u = genU.sample(rng)
        var v = genV.sample(rng)
        if (!(u > p1)) {
            y = f64ToI64(xM - p1 * v + u)
            break
        }

        if (!(u > p2)) {
            val x = xL + (u - p1) / c
            v = v * c + 1.0 - abs(x - xM) / p1
            if (v > 1.0) {
                continue
            } else {
                y = f64ToI64(x)
            }
        } else if (!(u > p3)) {
            y = f64ToI64(xL + ln(v) / lambdaL)
            if (y < 0L) {
                continue
            } else {
                v *= (u - p2) * lambdaL
            }
        } else {
            y = f64ToI64(xR - ln(v) / lambdaR)
            if (y > 0L && y.toULong() > btpe.n) {
                continue
            } else {
                v *= (u - p3) * lambdaR
            }
        }

        val k = abs(y - m)
        if (!(k > squeezeThreshold && k.toDouble() < 0.5 * npq - 1.0)) {
            val s = btpe.p / q
            val a = s * (n + 1.0)
            var f = 1.0
            when {
                m < y -> {
                    var i = m
                    while (true) {
                        i += 1L
                        f *= a / i.toDouble() - s
                        if (i == y) {
                            break
                        }
                    }
                }

                m > y -> {
                    var i = y
                    while (true) {
                        i += 1L
                        f /= a / i.toDouble() - s
                        if (i == m) {
                            break
                        }
                    }
                }

                else -> {}
            }
            if (v > f) {
                continue
            } else {
                break
            }
        }

        val kFloat = k.toDouble()
        val rho = (kFloat / npq) * ((kFloat * (kFloat / 3.0 + 0.625) + 1.0 / 6.0) / npq + 0.5)
        val t = -0.5 * kFloat * kFloat / npq
        val alpha = ln(v)
        if (alpha < t - rho) {
            break
        }
        if (alpha > t + rho) {
            continue
        }

        val x1 = (y + 1L).toDouble()
        val f1 = (m + 1L).toDouble()
        val z = (f64ToI64(n) + 1L - m).toDouble()
        val w = (f64ToI64(n) - y + 1L).toDouble()

        if (alpha >
            xM * ln(f1 / x1) +
            (n - m.toDouble() + 0.5) * ln(z / w) +
            (y - m).toDouble() * ln(w * btpe.p / (x1 * q)) +
            btpeStirling(f1) +
            btpeStirling(z) -
            btpeStirling(x1) -
            btpeStirling(w)
        ) {
            continue
        }

        break
    }
    check(y >= 0L)
    val yU64 = y.toULong()
    return if (flipped) {
        btpe.n - yU64
    } else {
        yU64
    }
}

/**
 * The binomial distribution `Binomial(n, p)`.
 */
public data class Binomial internal constructor(
    private val method: BinomialMethod,
    public val error: BinomialError? = null,
) : Distribution<ULong> {
    public companion object {
        /**
         * Construct a new `Binomial` with the given shape parameters `n` (number
         * of trials) and `p` (probability of success).
         */
        public fun new(
            n: ULong,
            p: Double,
        ): Binomial {
            if (!(p >= 0.0)) {
                return Binomial(BinomialMethod.Constant(0uL), BinomialError.ProbabilityTooSmall)
            }
            if (!(p <= 1.0)) {
                return Binomial(BinomialMethod.Constant(0uL), BinomialError.ProbabilityTooLarge)
            }
            if (p == 0.0) {
                return Binomial(BinomialMethod.Constant(0uL), null)
            }
            if (p == 1.0) {
                return Binomial(BinomialMethod.Constant(n), null)
            }

            val flipped = p > 0.5
            val pAdj = if (flipped) 1.0 - p else p
            val binvThreshold = 10.0
            val np = n.toDouble() * pAdj
            val method =
                if (np < binvThreshold) {
                    val q = 1.0 - pAdj
                    if (q == 1.0) {
                        BinomialMethod.Poisson(KnuthMethod.new(np))
                    } else {
                        val s = pAdj / q
                        BinomialMethod.Binv(
                            BinomialBinv(
                                r = q.pow(n.toDouble()),
                                s = s,
                                a = (n.toDouble() + 1.0) * s,
                                n = n,
                            ),
                            flipped,
                        )
                    }
                } else {
                    val q = 1.0 - pAdj
                    val npq = np * q
                    val p1 = floor(2.195 * sqrt(npq) - 4.6 * q) + 0.5
                    val fM = np + pAdj
                    val m = f64ToI64(fM)
                    BinomialMethod.Btpe(BinomialBtpe(n, pAdj, m, p1), flipped)
                }
            return Binomial(method, null)
        }
    }

    public fun isOk(): Boolean = error == null

    public fun isErr(): Boolean = error != null

    public fun unwrap(): Binomial {
        if (error != null) {
            throw IllegalStateException(error.toString())
        }
        return this
    }

    public fun unwrapErr(): BinomialError = error ?: throw IllegalStateException("Called unwrapErr on an Ok value")

    override fun sample(rng: Rng): ULong =
        when (val m = method) {
            is BinomialMethod.Binv -> sampleBinv(m.binv, m.flipped, rng)
            is BinomialMethod.Btpe -> sampleBtpe(m.btpe, m.flipped, rng)
            is BinomialMethod.Poisson -> m.knuth.sample(rng).toULong()
            is BinomialMethod.Constant -> m.value
        }
}
