// port-lint: source src/weighted/weighted_tree.rs
package io.github.kotlinmania.randdistr.weighted

import io.github.kotlinmania.rand.Rng
import io.github.kotlinmania.rand.distr.Distribution
import io.github.kotlinmania.rand.distr.Uniform

/**
 * Outcome of [WeightedTreeIndex.trySample].
 */
public class WeightedTreeSampleOutcome internal constructor(
    public val value: Int?,
    public val error: WeightedError?,
) {
    init {
        require((value == null) != (error == null)) {
            "WeightedTreeSampleOutcome must carry exactly one of value or error"
        }
    }

    public fun isOk(): Boolean = value != null

    public fun isErr(): Boolean = error != null

    public fun unwrap(): Int = value ?: throw IllegalStateException("Called unwrap on an Err value: $error")

    public fun unwrapErr(): WeightedError = error ?: throw IllegalStateException("Called unwrapErr on an Ok value")
}

internal fun WeightedError?.unwrap() {
    if (this != null) {
        throw IllegalStateException(this.toString())
    }
}

internal fun WeightedError?.unwrapErr(): WeightedError = this ?: throw IllegalStateException("Called unwrapErr on an Ok value")

/**
 * A distribution using weighted sampling to pick a discretely selected item,
 * structured as a tree optimized for frequent updates of the weights.
 */
public class WeightedTreeIndex internal constructor(
    private val subtotals: MutableList<Double>,
    private val integerMode: Boolean = false,
    public val error: WeightedError? = null,
) : Distribution<Int> {
    public companion object {
        private const val INT_MAX_AS_DOUBLE: Double = 2147483647.0

        private fun checkedAdd(
            a: Double,
            b: Double,
            integerMode: Boolean,
        ): Double? {
            val res = a + b
            if (integerMode) {
                if (res > INT_MAX_AS_DOUBLE || res < -2147483648.0) {
                    return null
                }
            }
            return res
        }

        /**
         * Creates a new [WeightedTreeIndex] from an array of [Double] weights.
         */
        public fun new(weights: DoubleArray): WeightedTreeIndex {
            val list = MutableList(weights.size) { weights[it] }
            for (weight in list) {
                if (!(weight >= 0.0)) {
                    return WeightedTreeIndex(mutableListOf(), false, WeightedError.InvalidWeight)
                }
            }
            val n = list.size
            for (i in (n - 1) downTo 1) {
                val w = list[i]
                val parent = (i - 1) / 2
                val added =
                    checkedAdd(list[parent], w, false)
                        ?: return WeightedTreeIndex(mutableListOf(), false, WeightedError.Overflow)
                list[parent] = added
            }
            return WeightedTreeIndex(list, false, null)
        }

        internal fun newInt(weights: IntArray): WeightedTreeIndex {
            val list = MutableList(weights.size) { weights[it].toDouble() }
            for (weight in list) {
                if (!(weight >= 0.0)) {
                    return WeightedTreeIndex(mutableListOf(), true, WeightedError.InvalidWeight)
                }
            }
            val n = list.size
            for (i in (n - 1) downTo 1) {
                val w = list[i]
                val parent = (i - 1) / 2
                val added =
                    checkedAdd(list[parent], w, true)
                        ?: return WeightedTreeIndex(mutableListOf(), true, WeightedError.Overflow)
                list[parent] = added
            }
            return WeightedTreeIndex(list, true, null)
        }
    }

    public fun isOk(): Boolean = error == null

    public fun isErr(): Boolean = error != null

    public fun unwrap(): WeightedTreeIndex {
        if (error != null) {
            throw IllegalStateException(error.toString())
        }
        return this
    }

    public fun unwrapErr(): WeightedError = error ?: throw IllegalStateException("Called unwrapErr on an Ok value")

    /**
     * Returns `true` if the tree contains no weights.
     */
    public fun isEmpty(): Boolean = subtotals.isEmpty()

    /**
     * Returns the number of weights.
     */
    public fun len(): Int = subtotals.size

    /**
     * Returns `true` if we can sample.
     */
    public fun isValid(): Boolean {
        val first = subtotals.firstOrNull() ?: return false
        return first > 0.0
    }

    /**
     * Gets the weight at an index.
     */
    public fun get(index: Int): Double {
        val leftIndex = 2 * index + 1
        val rightIndex = 2 * index + 2
        var w = subtotals[index]
        w -= subtotal(leftIndex)
        w -= subtotal(rightIndex)
        return w
    }

    /**
     * Removes the last weight and returns it, or `null` if it is empty.
     */
    public fun pop(): Double? {
        if (subtotals.isEmpty()) return null
        val weight = subtotals.removeAt(subtotals.size - 1)
        var index = len()
        while (index != 0) {
            index = (index - 1) / 2
            subtotals[index] = subtotals[index] - weight
        }
        return weight
    }

    /**
     * Appends a new weight at the end.
     */
    public fun push(weight: Double): WeightedError? {
        if (!(weight >= 0.0)) {
            return WeightedError.InvalidWeight
        }
        val first = subtotals.firstOrNull()
        if (first != null) {
            if (checkedAdd(first, weight, integerMode) == null) {
                return WeightedError.Overflow
            }
        }
        var index = len()
        subtotals.add(weight)
        while (index != 0) {
            index = (index - 1) / 2
            subtotals[index] = checkedAdd(subtotals[index], weight, integerMode)
                ?: return WeightedError.Overflow
        }
        return null
    }

    /**
     * Updates the weight at an index.
     */
    public fun update(
        index: Int,
        weight: Double,
    ): WeightedError? {
        if (!(weight >= 0.0)) {
            return WeightedError.InvalidWeight
        }
        var idx = index
        val oldWeight = get(idx)
        if (weight > oldWeight) {
            val difference = weight - oldWeight
            val first = subtotals.firstOrNull()
            if (first != null) {
                if (checkedAdd(first, difference, integerMode) == null) {
                    return WeightedError.Overflow
                }
            }
            subtotals[idx] = checkedAdd(subtotals[idx], difference, integerMode)
                ?: return WeightedError.Overflow
            while (idx != 0) {
                idx = (idx - 1) / 2
                subtotals[idx] = checkedAdd(subtotals[idx], difference, integerMode)
                    ?: return WeightedError.Overflow
            }
        } else if (weight < oldWeight) {
            val difference = oldWeight - weight
            subtotals[idx] = subtotals[idx] - difference
            while (idx != 0) {
                idx = (idx - 1) / 2
                subtotals[idx] = subtotals[idx] - difference
            }
        }
        return null
    }

    private fun subtotal(index: Int): Double =
        if (index < subtotals.size) {
            subtotals[index]
        } else {
            0.0
        }

    /**
     * Samples a randomly selected index from the weighted distribution.
     */
    public fun trySample(rng: Rng): WeightedTreeSampleOutcome {
        val totalWeight = subtotals.firstOrNull() ?: 0.0
        if (totalWeight == 0.0) {
            return WeightedTreeSampleOutcome(null, WeightedError.InsufficientNonZero)
        }
        var targetWeight =
            if (integerMode) {
                Uniform
                    .new(0, totalWeight.toInt())
                    .unwrap()
                    .sample(rng)
                    .toDouble()
            } else {
                Uniform.new(0.0, totalWeight).unwrap().sample(rng)
            }
        var index = 0
        while (true) {
            val leftIndex = 2 * index + 1
            val leftSubtotal = subtotal(leftIndex)
            if (targetWeight < leftSubtotal) {
                index = leftIndex
                continue
            }
            targetWeight -= leftSubtotal

            val rightIndex = 2 * index + 2
            val rightSubtotal = subtotal(rightIndex)
            if (targetWeight < rightSubtotal) {
                index = rightIndex
                continue
            }
            targetWeight -= rightSubtotal

            break
        }
        check(targetWeight >= 0.0)
        check(targetWeight < get(index))
        return WeightedTreeSampleOutcome(index, null)
    }

    override fun sample(rng: Rng): Int = trySample(rng).unwrap()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is WeightedTreeIndex) return false
        return subtotals == other.subtotals && error == other.error
    }

    override fun hashCode(): Int {
        var result = subtotals.hashCode()
        result = 31 * result + (error?.hashCode() ?: 0)
        return result
    }
}
