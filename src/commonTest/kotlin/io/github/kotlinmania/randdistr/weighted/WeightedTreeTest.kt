// port-lint: tests src/weighted/weighted_tree.rs
package io.github.kotlinmania.randdistr.weighted

import io.github.kotlinmania.randdistr.randomDouble
import io.github.kotlinmania.randdistr.testRng
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedTreeTest {
    @Test
    fun testNoItemError() {
        val rng = testRng(0x9c9fa0b0580a7031uL)
        val tree = WeightedTreeIndex.new(doubleArrayOf()).unwrap()
        assertEquals(
            WeightedError.InsufficientNonZero,
            tree.trySample(rng).unwrapErr(),
        )
    }

    @Test
    fun testOverflowError() {
        assertEquals(
            WeightedError.Overflow,
            WeightedTreeIndex.newInt(intArrayOf(Int.MAX_VALUE, 2)).unwrapErr(),
        )
        val tree = WeightedTreeIndex.newInt(intArrayOf(Int.MAX_VALUE - 2, 1)).unwrap()
        assertEquals(WeightedError.Overflow, tree.push(3.0))
        assertEquals(WeightedError.Overflow, tree.update(1, 4.0))
        tree.update(1, 2.0).unwrap()
    }

    @Test
    fun testAllWeightsZeroError() {
        val tree = WeightedTreeIndex.new(doubleArrayOf(0.0, 0.0)).unwrap()
        val rng = testRng(0x9c9fa0b0580a7031uL)
        assertEquals(
            WeightedError.InsufficientNonZero,
            tree.trySample(rng).unwrapErr(),
        )
    }

    @Test
    fun testInvalidWeightError() {
        assertEquals(
            WeightedError.InvalidWeight,
            WeightedTreeIndex.newInt(intArrayOf(1, -1)).unwrapErr(),
        )
        val tree = WeightedTreeIndex.newInt(intArrayOf()).unwrap()
        assertEquals(WeightedError.InvalidWeight, tree.push(-1.0).unwrapErr())
        tree.push(1.0).unwrap()
        assertEquals(WeightedError.InvalidWeight, tree.update(0, -1.0).unwrapErr())
    }

    @Test
    fun testTreeModifications() {
        val tree = WeightedTreeIndex.newInt(intArrayOf(9, 1, 2)).unwrap()
        tree.push(3.0).unwrap()
        tree.push(5.0).unwrap()
        tree.update(0, 0.0).unwrap()
        assertEquals(5.0, tree.pop())
        val expected = WeightedTreeIndex.newInt(intArrayOf(0, 1, 2, 3)).unwrap()
        assertEquals(expected, tree)
    }

    @Test
    fun testSampleCountsMatchProbabilities() {
        val start = 1
        val end = 3
        val samples = 20
        val rng = testRng(0x9c9fa0b0580a7031uL)
        val initialWeights = DoubleArray(end) { rng.randomDouble() }
        val tree = WeightedTreeIndex.new(initialWeights).unwrap()
        var totalWeight = 0.0
        val weights = DoubleArray(end)
        for (i in 0 until end) {
            tree.update(i, i.toDouble()).unwrap()
            weights[i] = i.toDouble()
            totalWeight += i.toDouble()
        }
        for (i in 0 until start) {
            tree.update(i, 0.0).unwrap()
            weights[i] = 0.0
            totalWeight -= i.toDouble()
        }
        val counts = IntArray(end)
        repeat(samples) {
            val i = tree.sample(rng)
            counts[i] += 1
        }
        for (i in 0 until start) {
            assertEquals(0, counts[i])
        }
        for (i in start until end) {
            val diff = counts[i].toDouble() / samples.toDouble() - weights[i] / totalWeight
            assertTrue(abs(diff) < 0.05)
        }
    }
}
