// port-lint: tests src/weighted/weighted_alias.rs
package io.github.kotlinmania.randdistr.weighted

import io.github.kotlinmania.rand.distr.Uniform
import io.github.kotlinmania.randdistr.testRng
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedAliasTest {
    @Test
    fun testWeightedIndexF32() {
        val numWeights = 10
        val zeroWeightIndex = 3
        val numSamples = 15000
        val rng = testRng(0x9c9fa0b0580a7031uL)

        val randomWeightDistribution = Uniform.newInclusive(0.0f, Float.MAX_VALUE / numWeights.toFloat()).unwrap()
        val weights = FloatArray(numWeights) { rng.sample(randomWeightDistribution) }
        weights[zeroWeightIndex] = 0.0f

        val weightSum = pairwiseSumFloat(weights)
        val expectedCounts =
            DoubleArray(numWeights) {
                weights[it].toDouble() / weightSum.toDouble() * numSamples.toDouble()
            }
        val weightDistribution = WeightedAliasIndex.newFloat(weights).unwrap()

        val counts = IntArray(numWeights)
        repeat(numSamples) {
            counts[rng.sample(weightDistribution)] += 1
        }

        assertEquals(0, counts[zeroWeightIndex])
        for (i in 0 until numWeights) {
            val difference = abs(counts[i].toDouble() - expectedCounts[i])
            val maxAllowedDifference = numSamples.toDouble() / numWeights.toDouble() * 0.1
            assertTrue(difference <= maxAllowedDifference)
        }

        assertEquals(
            WeightedError.InvalidInput,
            WeightedAliasIndex.newFloat(floatArrayOf()).unwrapErr(),
        )
        assertEquals(
            WeightedError.InsufficientNonZero,
            WeightedAliasIndex.newFloat(floatArrayOf(0.0f)).unwrapErr(),
        )
        assertEquals(
            WeightedError.InvalidWeight,
            WeightedAliasIndex.newFloat(floatArrayOf(Float.MAX_VALUE, Float.MAX_VALUE)).unwrapErr(),
        )

        assertEquals(
            WeightedError.InvalidWeight,
            WeightedAliasIndex.newFloat(floatArrayOf(Float.POSITIVE_INFINITY)).unwrapErr(),
        )
        assertEquals(
            WeightedError.InsufficientNonZero,
            WeightedAliasIndex.newFloat(floatArrayOf(-0.0f)).unwrapErr(),
        )
        assertEquals(
            WeightedError.InvalidWeight,
            WeightedAliasIndex.newFloat(floatArrayOf(-1.0f)).unwrapErr(),
        )
        assertEquals(
            WeightedError.InvalidWeight,
            WeightedAliasIndex.newFloat(floatArrayOf(Float.NEGATIVE_INFINITY)).unwrapErr(),
        )
        assertEquals(
            WeightedError.InvalidWeight,
            WeightedAliasIndex.newFloat(floatArrayOf(Float.NaN)).unwrapErr(),
        )
    }

    @Test
    fun testWeightedIndexInt() {
        val numWeights = 10
        val zeroWeightIndex = 3
        val numSamples = 15000
        val rng = testRng(0x9c9fa0b0580a7031uL)

        val randomWeightDistribution = Uniform.newInclusive(0, Int.MAX_VALUE / numWeights).unwrap()
        val weights = IntArray(numWeights) { rng.sample(randomWeightDistribution) }
        weights[zeroWeightIndex] = 0

        val weightSum = weights.sum()
        val expectedCounts =
            DoubleArray(numWeights) {
                weights[it].toDouble() / weightSum.toDouble() * numSamples.toDouble()
            }
        val weightDistribution = WeightedAliasIndex.newInt(weights).unwrap()

        val counts = IntArray(numWeights)
        repeat(numSamples) {
            counts[rng.sample(weightDistribution)] += 1
        }

        assertEquals(0, counts[zeroWeightIndex])
        for (i in 0 until numWeights) {
            val difference = abs(counts[i].toDouble() - expectedCounts[i])
            val maxAllowedDifference = numSamples.toDouble() / numWeights.toDouble() * 0.1
            assertTrue(difference <= maxAllowedDifference)
        }

        assertEquals(
            WeightedError.InvalidWeight,
            WeightedAliasIndex.newInt(intArrayOf(-1)).unwrapErr(),
        )
        assertEquals(
            WeightedError.InvalidWeight,
            WeightedAliasIndex.newInt(intArrayOf(Int.MIN_VALUE)).unwrapErr(),
        )
    }

    // Rust u128 primitive integer type has no native primitive representation in Kotlin.
    // Rust i128 primitive integer type has no native primitive representation in Kotlin.

    @Test
    fun testWeightedIndexU8() {
        val numWeights = 10
        val zeroWeightIndex = 3
        val numSamples = 15000
        val rng = testRng(0x9c9fa0b0580a7031uL)

        val randomWeightDistribution = Uniform.newInclusive(0, 255 / numWeights).unwrap()
        val weights = UByteArray(numWeights) { rng.sample(randomWeightDistribution).toUByte() }
        weights[zeroWeightIndex] = 0u

        val weightSum = weights.sumOf { it.toInt() }
        val expectedCounts =
            DoubleArray(numWeights) {
                weights[it].toDouble() / weightSum.toDouble() * numSamples.toDouble()
            }
        val weightDistribution = WeightedAliasIndex.newUByte(weights).unwrap()

        val counts = IntArray(numWeights)
        repeat(numSamples) {
            counts[rng.sample(weightDistribution)] += 1
        }

        assertEquals(0, counts[zeroWeightIndex])
        for (i in 0 until numWeights) {
            val difference = abs(counts[i].toDouble() - expectedCounts[i])
            val maxAllowedDifference = numSamples.toDouble() / numWeights.toDouble() * 0.1
            assertTrue(difference <= maxAllowedDifference)
        }

        assertEquals(
            WeightedError.InvalidInput,
            WeightedAliasIndex.newUByte(ubyteArrayOf()).unwrapErr(),
        )
        assertEquals(
            WeightedError.InsufficientNonZero,
            WeightedAliasIndex.newUByte(ubyteArrayOf(0u)).unwrapErr(),
        )
        assertEquals(
            WeightedError.InvalidWeight,
            WeightedAliasIndex.newUByte(ubyteArrayOf(255u, 255u)).unwrapErr(),
        )
    }

    @Test
    fun testWeightedIndexI8() {
        val numWeights = 10
        val zeroWeightIndex = 3
        val numSamples = 15000
        val rng = testRng(0x9c9fa0b0580a7031uL)

        val randomWeightDistribution = Uniform.newInclusive(0, 127 / numWeights).unwrap()
        val weights = ByteArray(numWeights) { rng.sample(randomWeightDistribution).toByte() }
        weights[zeroWeightIndex] = 0

        val weightSum = weights.sumOf { it.toInt() }
        val expectedCounts =
            DoubleArray(numWeights) {
                weights[it].toDouble() / weightSum.toDouble() * numSamples.toDouble()
            }
        val weightDistribution = WeightedAliasIndex.newByte(weights).unwrap()

        val counts = IntArray(numWeights)
        repeat(numSamples) {
            counts[rng.sample(weightDistribution)] += 1
        }

        assertEquals(0, counts[zeroWeightIndex])
        for (i in 0 until numWeights) {
            val difference = abs(counts[i].toDouble() - expectedCounts[i])
            val maxAllowedDifference = numSamples.toDouble() / numWeights.toDouble() * 0.1
            assertTrue(difference <= maxAllowedDifference)
        }

        assertEquals(
            WeightedError.InvalidWeight,
            WeightedAliasIndex.newByte(byteArrayOf((-1).toByte())).unwrapErr(),
        )
        assertEquals(
            WeightedError.InvalidWeight,
            WeightedAliasIndex.newByte(byteArrayOf(Byte.MIN_VALUE)).unwrapErr(),
        )
    }

    @Test
    fun valueStability() {
        val distrInt = WeightedAliasIndex.newInt(intArrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1)).unwrap()
        val rngInt = testRng(0x9c9fa0b0580a7031uL)
        val bufInt = IntArray(10) { rngInt.sample(distrInt) }
        assertContentEquals(intArrayOf(6, 5, 7, 5, 8, 7, 6, 2, 3, 7), bufInt)

        val distrFloat = WeightedAliasIndex.newFloat(floatArrayOf(0.7f, 0.1f, 0.1f, 0.1f)).unwrap()
        val rngFloat = testRng(0x9c9fa0b0580a7031uL)
        val bufFloat = IntArray(10) { rngFloat.sample(distrFloat) }
        assertContentEquals(intArrayOf(2, 0, 0, 0, 0, 0, 0, 0, 1, 3), bufFloat)

        val distrDouble = WeightedAliasIndex.new(doubleArrayOf(1.0, 0.999, 0.998, 0.997)).unwrap()
        val rngDouble = testRng(0x9c9fa0b0580a7031uL)
        val bufDouble = IntArray(10) { rngDouble.sample(distrDouble) }
        assertContentEquals(intArrayOf(2, 1, 2, 3, 2, 1, 3, 2, 1, 1), bufDouble)
    }
}
