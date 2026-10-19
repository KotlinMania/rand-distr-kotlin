// port-lint: source src/weighted/weighted_alias.rs
package io.github.kotlinmania.randdistr.weighted

import io.github.kotlinmania.rand.Rng
import io.github.kotlinmania.rand.distr.Distribution
import io.github.kotlinmania.rand.distr.Uniform

/**
 * Error type returned from weighted index distributions.
 */
public enum class WeightedError {
    /** The input weight sequence is empty, too long, or wrongly ordered. */
    InvalidInput,

    /** A weight is negative, too large for the distribution, or not a valid number. */
    InvalidWeight,

    /** Not enough non-zero weights are available to sample values. */
    InsufficientNonZero,

    /** Overflow when calculating the sum of weights. */
    Overflow,
}

internal fun pairwiseSum(
    values: DoubleArray,
    start: Int = 0,
    end: Int = values.size,
): Double {
    val len = end - start
    if (len <= 32) {
        var acc = 0.0
        for (i in start until end) {
            acc += values[i]
        }
        return acc
    }
    val mid = start + len / 2
    return pairwiseSum(values, start, mid) + pairwiseSum(values, mid, end)
}

internal fun pairwiseSumFloat(
    values: FloatArray,
    start: Int = 0,
    end: Int = values.size,
): Float {
    val len = end - start
    if (len <= 32) {
        var acc = 0.0f
        for (i in start until end) {
            acc += values[i]
        }
        return acc
    }
    val mid = start + len / 2
    return pairwiseSumFloat(values, start, mid) + pairwiseSumFloat(values, mid, end)
}

private class Aliases(
    size: Int,
) {
    val aliases: UIntArray = UIntArray(size)
    var smallsHead: UInt = UInt.MAX_VALUE
    var bigsHead: UInt = UInt.MAX_VALUE

    fun pushSmall(idx: UInt) {
        aliases[idx.toInt()] = smallsHead
        smallsHead = idx
    }

    fun pushBig(idx: UInt) {
        aliases[idx.toInt()] = bigsHead
        bigsHead = idx
    }

    fun popSmall(): UInt {
        val popped = smallsHead
        smallsHead = aliases[popped.toInt()]
        return popped
    }

    fun popBig(): UInt {
        val popped = bigsHead
        bigsHead = aliases[popped.toInt()]
        return popped
    }

    fun smallsIsEmpty(): Boolean = smallsHead == UInt.MAX_VALUE

    fun bigsIsEmpty(): Boolean = bigsHead == UInt.MAX_VALUE

    fun setAlias(
        idx: UInt,
        alias: UInt,
    ) {
        aliases[idx.toInt()] = alias
    }
}

/**
 * A distribution using weighted sampling to pick a discretely selected item.
 */
public class WeightedAliasIndex internal constructor(
    private val aliases: UIntArray,
    private val uniformIndex: Uniform<UInt>?,
    private val checkNoAlias: (Rng, Int) -> Boolean,
    public val error: WeightedError? = null,
) : Distribution<Int> {
    public companion object {
        private fun err(e: WeightedError): WeightedAliasIndex = WeightedAliasIndex(UIntArray(0), null, { _, _ -> false }, e)

        /**
         * Creates a new [WeightedAliasIndex] from [Double] weights.
         */
        public fun new(weights: DoubleArray): WeightedAliasIndex {
            val n = weights.size
            if (n == 0) {
                return err(WeightedError.InvalidInput)
            }
            val nU32 = n.toUInt()
            val maxWeightSize = Double.MAX_VALUE / n.toDouble()
            for (w in weights) {
                if (!(0.0 <= w && w <= maxWeightSize)) {
                    return err(WeightedError.InvalidWeight)
                }
            }

            var weightSum = pairwiseSum(weights)
            if (weightSum > Double.MAX_VALUE) {
                weightSum = Double.MAX_VALUE
            }
            if (weightSum == 0.0) {
                return err(WeightedError.InsufficientNonZero)
            }

            val nConverted = n.toDouble()
            val noAliasOdds = weights.copyOf()
            for (i in noAliasOdds.indices) {
                var odds = noAliasOdds[i] * nConverted
                if (odds > Double.MAX_VALUE) {
                    odds = Double.MAX_VALUE
                }
                noAliasOdds[i] = odds
            }

            val aliasBuilder = Aliases(n)
            for (index in noAliasOdds.indices) {
                if (noAliasOdds[index] < weightSum) {
                    aliasBuilder.pushSmall(index.toUInt())
                } else {
                    aliasBuilder.pushBig(index.toUInt())
                }
            }

            while (!aliasBuilder.smallsIsEmpty() && !aliasBuilder.bigsIsEmpty()) {
                val s = aliasBuilder.popSmall()
                val b = aliasBuilder.popBig()
                aliasBuilder.setAlias(s, b)
                val bIdx = b.toInt()
                val sIdx = s.toInt()
                noAliasOdds[bIdx] = noAliasOdds[bIdx] - weightSum + noAliasOdds[sIdx]
                if (noAliasOdds[bIdx] < weightSum) {
                    aliasBuilder.pushSmall(b)
                } else {
                    aliasBuilder.pushBig(b)
                }
            }

            while (!aliasBuilder.smallsIsEmpty()) {
                noAliasOdds[aliasBuilder.popSmall().toInt()] = weightSum
            }
            while (!aliasBuilder.bigsIsEmpty()) {
                noAliasOdds[aliasBuilder.popBig().toInt()] = weightSum
            }

            val uniformIndex = Uniform.new(0u, nU32).unwrap()
            val uniformWithinWeightSum = Uniform.new(0.0, weightSum).unwrap()
            return WeightedAliasIndex(
                aliases = aliasBuilder.aliases,
                uniformIndex = uniformIndex,
                checkNoAlias = { rng, candidate ->
                    rng.sample(uniformWithinWeightSum) < noAliasOdds[candidate]
                },
                error = null,
            )
        }

        internal fun newFloat(weights: FloatArray): WeightedAliasIndex {
            val n = weights.size
            if (n == 0) {
                return err(WeightedError.InvalidInput)
            }
            val nU32 = n.toUInt()
            val maxWeightSize = Float.MAX_VALUE / n.toFloat()
            for (w in weights) {
                if (!(0.0f <= w && w <= maxWeightSize)) {
                    return err(WeightedError.InvalidWeight)
                }
            }

            var weightSum = pairwiseSumFloat(weights)
            if (weightSum > Float.MAX_VALUE) {
                weightSum = Float.MAX_VALUE
            }
            if (weightSum == 0.0f) {
                return err(WeightedError.InsufficientNonZero)
            }

            val nConverted = n.toFloat()
            val noAliasOdds = weights.copyOf()
            for (i in noAliasOdds.indices) {
                var odds = noAliasOdds[i] * nConverted
                if (odds > Float.MAX_VALUE) {
                    odds = Float.MAX_VALUE
                }
                noAliasOdds[i] = odds
            }

            val aliasBuilder = Aliases(n)
            for (index in noAliasOdds.indices) {
                if (noAliasOdds[index] < weightSum) {
                    aliasBuilder.pushSmall(index.toUInt())
                } else {
                    aliasBuilder.pushBig(index.toUInt())
                }
            }

            while (!aliasBuilder.smallsIsEmpty() && !aliasBuilder.bigsIsEmpty()) {
                val s = aliasBuilder.popSmall()
                val b = aliasBuilder.popBig()
                aliasBuilder.setAlias(s, b)
                val bIdx = b.toInt()
                val sIdx = s.toInt()
                noAliasOdds[bIdx] = noAliasOdds[bIdx] - weightSum + noAliasOdds[sIdx]
                if (noAliasOdds[bIdx] < weightSum) {
                    aliasBuilder.pushSmall(b)
                } else {
                    aliasBuilder.pushBig(b)
                }
            }

            while (!aliasBuilder.smallsIsEmpty()) {
                noAliasOdds[aliasBuilder.popSmall().toInt()] = weightSum
            }
            while (!aliasBuilder.bigsIsEmpty()) {
                noAliasOdds[aliasBuilder.popBig().toInt()] = weightSum
            }

            val uniformIndex = Uniform.new(0u, nU32).unwrap()
            val uniformWithinWeightSum = Uniform.new(0.0f, weightSum).unwrap()
            return WeightedAliasIndex(
                aliases = aliasBuilder.aliases,
                uniformIndex = uniformIndex,
                checkNoAlias = { rng, candidate ->
                    rng.sample(uniformWithinWeightSum) < noAliasOdds[candidate]
                },
                error = null,
            )
        }

        internal fun newInt(weights: IntArray): WeightedAliasIndex {
            val n = weights.size
            if (n == 0) {
                return err(WeightedError.InvalidInput)
            }
            val nU32 = n.toUInt()
            val maxWeightSize = Int.MAX_VALUE / n
            for (w in weights) {
                if (!(0 <= w && w <= maxWeightSize)) {
                    return err(WeightedError.InvalidWeight)
                }
            }

            var weightSum = 0
            for (w in weights) {
                weightSum += w
            }
            if (weightSum == 0) {
                return err(WeightedError.InsufficientNonZero)
            }

            val noAliasOdds = IntArray(n) { weights[it] * n }
            val aliasBuilder = Aliases(n)
            for (index in noAliasOdds.indices) {
                if (noAliasOdds[index] < weightSum) {
                    aliasBuilder.pushSmall(index.toUInt())
                } else {
                    aliasBuilder.pushBig(index.toUInt())
                }
            }

            while (!aliasBuilder.smallsIsEmpty() && !aliasBuilder.bigsIsEmpty()) {
                val s = aliasBuilder.popSmall()
                val b = aliasBuilder.popBig()
                aliasBuilder.setAlias(s, b)
                val bIdx = b.toInt()
                val sIdx = s.toInt()
                noAliasOdds[bIdx] = noAliasOdds[bIdx] - weightSum + noAliasOdds[sIdx]
                if (noAliasOdds[bIdx] < weightSum) {
                    aliasBuilder.pushSmall(b)
                } else {
                    aliasBuilder.pushBig(b)
                }
            }

            while (!aliasBuilder.smallsIsEmpty()) {
                noAliasOdds[aliasBuilder.popSmall().toInt()] = weightSum
            }
            while (!aliasBuilder.bigsIsEmpty()) {
                noAliasOdds[aliasBuilder.popBig().toInt()] = weightSum
            }

            val uniformIndex = Uniform.new(0u, nU32).unwrap()
            val uniformWithinWeightSum = Uniform.new(0, weightSum).unwrap()
            return WeightedAliasIndex(
                aliases = aliasBuilder.aliases,
                uniformIndex = uniformIndex,
                checkNoAlias = { rng, candidate ->
                    rng.sample(uniformWithinWeightSum) < noAliasOdds[candidate]
                },
                error = null,
            )
        }

        internal fun newByte(weights: ByteArray): WeightedAliasIndex {
            val n = weights.size
            if (n == 0) {
                return err(WeightedError.InvalidInput)
            }
            val maxWeightSize = (Byte.MAX_VALUE / n).toByte()
            for (w in weights) {
                if (!(0 <= w && w <= maxWeightSize)) {
                    return err(WeightedError.InvalidWeight)
                }
            }
            val intWeights = IntArray(n) { weights[it].toInt() }
            return newInt(intWeights)
        }

        internal fun newUByte(weights: UByteArray): WeightedAliasIndex {
            val n = weights.size
            if (n == 0) {
                return err(WeightedError.InvalidInput)
            }
            val maxWeightSize = 255 / n
            for (w in weights) {
                if (w.toInt() > maxWeightSize) {
                    return err(WeightedError.InvalidWeight)
                }
            }
            val intWeights = IntArray(n) { weights[it].toInt() }
            return newInt(intWeights)
        }
    }

    public fun isOk(): Boolean = error == null

    public fun isErr(): Boolean = error != null

    public fun unwrap(): WeightedAliasIndex {
        if (error != null) {
            throw IllegalStateException(error.toString())
        }
        return this
    }

    public fun unwrapErr(): WeightedError = error ?: throw IllegalStateException("Called unwrapErr on an Ok value")

    override fun sample(rng: Rng): Int {
        val uIdx = uniformIndex ?: throw IllegalStateException("Invalid WeightedAliasIndex")
        val candidate = rng.sample(uIdx).toInt()
        return if (checkNoAlias(rng, candidate)) {
            candidate
        } else {
            aliases[candidate].toInt()
        }
    }
}
