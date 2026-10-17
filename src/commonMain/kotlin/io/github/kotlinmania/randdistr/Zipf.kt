// port-lint: source src/zipf.rs
package io.github.kotlinmania.randdistr

import io.github.kotlinmania.rand.Rng
import io.github.kotlinmania.rand.distr.Distribution
import kotlin.math.exp
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.pow

/**
 * Error type returned from [Zipf.new].
 */
public enum class ZipfError {
    /** `s < 0` or `nan`. */
    STooSmall,

    /** `n < 1`. */
    NTooSmall,
    ;

    override fun toString(): String =
        when (this) {
            STooSmall -> "s < 0 or is NaN in Zipf distribution"
            NTooSmall -> "n < 1 in Zipf distribution"
        }
}

/**
 * Samples integers according to the Zipf distribution.
 */
public data class Zipf internal constructor(
    private val s: Double,
    private val t: Double,
    private val q: Double,
    private val nVal: Double = 0.0,
    public val error: ZipfError? = null,
) : Distribution<Double> {
    public companion object {
        /**
         * Construct a new `Zipf` distribution for a set with `n` elements and a
         * frequency rank exponent `s`.
         */
        public fun new(
            n: Double,
            s: Double,
        ): Zipf {
            if (!(s >= 0.0)) {
                return Zipf(0.0, 0.0, 0.0, 0.0, ZipfError.STooSmall)
            }
            if (n < 1.0 || n.isNaN()) {
                return Zipf(0.0, 0.0, 0.0, 0.0, ZipfError.NTooSmall)
            }
            val q =
                if (s != 1.0) {
                    1.0 / (1.0 - s)
                } else {
                    0.0
                }
            val t =
                if (s != 1.0) {
                    (n.pow(1.0 - s) - s) * q
                } else {
                    1.0 + ln(n)
                }
            return Zipf(s, t, q, n, null)
        }
    }

    private fun invCdf(p: Double): Double {
        val one = 1.0
        val pt = p * t
        return if (pt <= one) {
            pt
        } else if (s != one) {
            (pt * (one - s) + s).pow(q)
        } else {
            exp(pt - one)
        }
    }

    public fun isOk(): Boolean = error == null

    public fun isErr(): Boolean = error != null

    public fun unwrap(): Zipf {
        if (error != null) {
            throw IllegalStateException(error.toString())
        }
        return this
    }

    public fun unwrapErr(): ZipfError = error ?: throw IllegalStateException("Called unwrapErr on an Ok value")

    override fun sample(rng: Rng): Double {
        val one = 1.0
        while (true) {
            val invB = invCdf(rng.randomDouble())
            val x = floor(invB + one)
            var ratio = x.pow(-s)
            if (x > one) {
                ratio *= invB.pow(s)
            }
            val y = rng.randomDouble()
            if (y < ratio) {
                return x
            }
        }
    }

    internal fun sampleFloat(rng: Rng): Float {
        val sf = s.toFloat()
        val nf = nVal.toFloat()
        val qf = if (sf != 1.0f) 1.0f / (1.0f - sf) else 0.0f
        val tf = if (sf != 1.0f) (nf.pow(1.0f - sf) - sf) * qf else 1.0f + ln(nf)

        fun invCdfF(p: Float): Float {
            val pt = p * tf
            return if (pt <= 1.0f) {
                pt
            } else if (sf != 1.0f) {
                (pt * (1.0f - sf) + sf).pow(qf)
            } else {
                exp(pt - 1.0f)
            }
        }
        while (true) {
            val invB = invCdfF(rng.randomFloat())
            val x = floor(invB + 1.0f)
            var ratio = x.pow(-sf)
            if (x > 1.0f) {
                ratio *= invB.pow(sf)
            }
            val y = rng.randomFloat()
            if (y < ratio) {
                return x
            }
        }
    }
}
