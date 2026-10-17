// port-lint: source src/geometric.rs
package io.github.kotlinmania.randdistr

import io.github.kotlinmania.rand.Rng
import io.github.kotlinmania.rand.distr.Distribution
import kotlin.math.pow

/**
 * Error type returned from [Geometric.new].
 */
public enum class GeometricError {
    /** `p < 0 || p > 1` or `nan` */
    InvalidProbability,
    ;

    override fun toString(): String =
        when (this) {
            InvalidProbability -> "p is NaN or outside the interval [0, 1] in geometric distribution"
        }
}

/**
 * The geometric distribution `Geometric(p)`.
 */
public data class Geometric internal constructor(
    private val p: Double,
    private val pi: Double,
    private val k: ULong,
    public val error: GeometricError? = null,
) : Distribution<ULong> {
    public companion object {
        /**
         * Construct a new `Geometric` with the given shape parameter `p`
         * (probability of success on each trial).
         */
        public fun new(p: Double): Geometric {
            if (!p.isFinite() || p < 0.0 || p > 1.0) {
                return Geometric(0.0, 0.0, 0uL, GeometricError.InvalidProbability)
            }
            if (p == 0.0 || p >= 2.0 / 3.0) {
                return Geometric(p, p, 0uL, null)
            }
            var k = 1uL
            var pi = (1.0 - p) * (1.0 - p)
            while (pi > 0.5) {
                k += 1uL
                pi *= pi
            }
            return Geometric(p, pi, k, null)
        }
    }

    public fun isOk(): Boolean = error == null

    public fun isErr(): Boolean = error != null

    public fun unwrap(): Geometric {
        if (error != null) {
            throw IllegalStateException(error.toString())
        }
        return this
    }

    public fun unwrapErr(): GeometricError = error ?: throw IllegalStateException("Called unwrapErr on an Ok value")

    override fun sample(rng: Rng): ULong {
        if (p >= 2.0 / 3.0) {
            var failures = 0uL
            while (true) {
                val u = rng.randomDouble()
                if (u <= p) {
                    break
                }
                failures += 1uL
            }
            return failures
        }

        if (p == 0.0) {
            return ULong.MAX_VALUE
        }

        var d = 0uL
        while (rng.randomDouble() < pi) {
            d += 1uL
        }

        val kShift = k.toInt()
        val mask = (1uL shl kShift) - 1uL
        var m: ULong
        while (true) {
            m = rng.nextU64() and mask
            val pReject =
                if (m <= Int.MAX_VALUE.toULong()) {
                    (1.0 - p).pow(m.toInt())
                } else {
                    (1.0 - p).pow(m.toDouble())
                }
            val u = rng.randomDouble()
            if (u < pReject) {
                break
            }
        }

        return (d shl kShift) + m
    }
}

/**
 * The standard geometric distribution `Geometric(0.5)`.
 */
public object StandardGeometric : Distribution<ULong> {
    override fun sample(rng: Rng): ULong {
        var result = 0uL
        while (true) {
            val x = rng.nextU64().countLeadingZeroBits().toULong()
            result += x
            if (x < 64uL) {
                break
            }
        }
        return result
    }
}
