// port-lint: source src/dirichlet.rs
package io.github.kotlinmania.randdistr

import io.github.kotlinmania.rand.Rng
import io.github.kotlinmania.rand.distr.Distribution
import kotlin.math.abs

internal data class DirichletFromGamma(
    val samplers: List<Gamma>,
) : Distribution<DoubleArray> {
    companion object {
        fun new(alpha: DoubleArray): DirichletFromGamma? {
            val gammaDists = ArrayList<Gamma>(alpha.size)
            for (a in alpha) {
                val dist = Gamma.new(a, 1.0)
                if (dist.isErr()) return null
                gammaDists.add(dist)
            }
            return DirichletFromGamma(gammaDists)
        }
    }

    override fun sample(rng: Rng): DoubleArray {
        val n = samplers.size
        val samples = DoubleArray(n)
        var sum = 0.0
        for (i in 0 until n) {
            val s = samplers[i].sample(rng)
            samples[i] = s
            sum += s
        }
        val invacc = 1.0 / sum
        for (i in 0 until n) {
            samples[i] *= invacc
        }
        return samples
    }
}

internal data class DirichletFromBeta(
    val samplers: List<Beta>,
) : Distribution<DoubleArray> {
    companion object {
        fun new(alpha: DoubleArray): DirichletFromBeta? {
            val n = alpha.size
            val alphaRevCsum = DoubleArray(n - 1) { alpha[n - 1] }
            for (k in 0 until (n - 2)) {
                alphaRevCsum[n - 3 - k] = alphaRevCsum[n - 2 - k] + alpha[n - 2 - k]
            }
            val betaDists = ArrayList<Beta>(n - 1)
            for (i in 0 until (n - 1)) {
                val dist = Beta.new(alpha[i], alphaRevCsum[i])
                if (dist.isErr()) return null
                betaDists.add(dist)
            }
            return DirichletFromBeta(betaDists)
        }
    }

    override fun sample(rng: Rng): DoubleArray {
        val n = samplers.size + 1
        val samples = DoubleArray(n)
        var acc = 1.0
        for (i in 0 until samplers.size) {
            val betaSample = samplers[i].sample(rng)
            samples[i] = acc * betaSample
            acc *= (1.0 - betaSample)
        }
        samples[n - 1] = acc
        return samples
    }
}

/**
 * Error type returned from [Dirichlet.new].
 */
public enum class DirichletError {
    /** `alpha.len() < 2`. */
    AlphaTooShort,

    /** `alpha <= 0.0` or `nan`. */
    AlphaTooSmall,

    /** `alpha` is subnormal. */
    AlphaSubnormal,

    /** `alpha` is infinite. */
    AlphaInfinite,

    /** Failed to create required Gamma distribution(s). */
    FailedToCreateGamma,

    /** Failed to create required Beta distribution(s). */
    FailedToCreateBeta,

    /** `size < 2`. */
    SizeTooSmall,
    ;

    override fun toString(): String =
        when (this) {
            AlphaTooShort, SizeTooSmall -> "less than 2 dimensions in Dirichlet distribution"
            AlphaTooSmall -> "alpha is not positive in Dirichlet distribution"
            AlphaSubnormal -> "alpha contains a subnormal value in Dirichlet distribution"
            AlphaInfinite -> "alpha contains an infinite value in Dirichlet distribution"
            FailedToCreateGamma -> "failed to create required Gamma distribution for Dirichlet distribution"
            FailedToCreateBeta -> "failed to create required Beta distribition for Dirichlet distribution"
        }
}

private fun isNormalDouble(x: Double): Boolean {
    val absX = abs(x)
    return absX >= 2.2250738585072014E-308 && absX <= Double.MAX_VALUE
}

/**
 * The Dirichlet distribution `Dirichlet(alpha)`.
 */
public data class Dirichlet internal constructor(
    internal val fromGamma: DirichletFromGamma?,
    internal val fromBeta: DirichletFromBeta?,
    public val error: DirichletError? = null,
) : Distribution<DoubleArray> {
    public companion object {
        /**
         * Construct a new `Dirichlet` with the given alpha parameter `alpha`.
         *
         * Requires `alpha.size >= 2`, and each value in `alpha` must be positive,
         * finite and not subnormal.
         */
        public fun new(alpha: DoubleArray): Dirichlet {
            if (alpha.size < 2) {
                return Dirichlet(null, null, DirichletError.AlphaTooShort)
            }
            for (ai in alpha) {
                if (!(ai > 0.0)) {
                    return Dirichlet(null, null, DirichletError.AlphaTooSmall)
                }
                if (ai.isInfinite()) {
                    return Dirichlet(null, null, DirichletError.AlphaInfinite)
                }
                if (!isNormalDouble(ai)) {
                    return Dirichlet(null, null, DirichletError.AlphaSubnormal)
                }
            }
            if (alpha.all { it <= 0.1 }) {
                val dist =
                    DirichletFromBeta.new(alpha)
                        ?: return Dirichlet(null, null, DirichletError.FailedToCreateBeta)
                return Dirichlet(null, dist, null)
            } else {
                val dist =
                    DirichletFromGamma.new(alpha)
                        ?: return Dirichlet(null, null, DirichletError.FailedToCreateGamma)
                return Dirichlet(dist, null, null)
            }
        }
    }

    public fun isOk(): Boolean = error == null

    public fun isErr(): Boolean = error != null

    public fun unwrap(): Dirichlet {
        if (error != null) {
            throw IllegalStateException(error.toString())
        }
        return this
    }

    public fun unwrapErr(): DirichletError = error ?: throw IllegalStateException("Called unwrapErr on an Ok value")

    override fun sample(rng: Rng): DoubleArray {
        fromGamma?.let { return it.sample(rng) }
        fromBeta?.let { return it.sample(rng) }
        throw IllegalStateException("Cannot sample from invalid Dirichlet distribution")
    }
}
