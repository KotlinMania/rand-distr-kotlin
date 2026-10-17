// port-lint: source src/utils.rs
package io.github.kotlinmania.randdistr

import io.github.kotlinmania.rand.Rng
import kotlin.math.abs

internal const val DOUBLE_EPSILON: Double = 2.220446049250313E-16
internal const val FLOAT_EPSILON: Float = 1.1920929E-7f

internal fun ULong.intoFloatWithExponent(exponent: Int): Double {
    val exponentBits = (1023 + exponent).toULong() shl 52
    return Double.fromBits((this or exponentBits).toLong())
}

internal fun UInt.intoFloatWithExponent(exponent: Int): Float {
    val exponentBits = (127 + exponent).toUInt() shl 23
    return Float.fromBits((this or exponentBits).toInt())
}

internal fun Rng.randomDouble(): Double {
    val scale = 1.0 / (1uL shl 53).toDouble()
    val value = nextU64()
    val shifted = value shr 11
    return scale * shifted.toDouble()
}

internal fun Rng.randomFloat(): Float {
    val scale = 1.0f / (1u shl 24).toFloat()
    val value = nextU32()
    val shifted = value shr 8
    return scale * shifted.toFloat()
}

internal fun Rng.sampleOpen01(): Double {
    val value = nextU64()
    val fraction = value shr 12
    return fraction.intoFloatWithExponent(0) - (1.0 - DOUBLE_EPSILON / 2.0)
}

internal fun Rng.sampleOpen01Float(): Float {
    val value = nextU32()
    val fraction = value shr 9
    return fraction.intoFloatWithExponent(0) - (1.0f - FLOAT_EPSILON / 2.0f)
}

internal fun Rng.sampleOpenClosed01(): Double {
    val scale = 1.0 / (1uL shl 53).toDouble()
    val value = nextU64()
    val shifted = value shr 11
    return scale * (shifted + 1uL).toDouble()
}

internal fun Rng.sampleOpenClosed01Float(): Float {
    val scale = 1.0f / (1u shl 24).toFloat()
    val value = nextU32()
    val shifted = value shr 8
    return scale * (shifted + 1u).toFloat()
}

/**
 * Sample a random number using the Ziggurat method (specifically the
 * ZIGNOR variant from Doornik 2005).
 */
internal inline fun ziggurat(
    rng: Rng,
    symmetric: Boolean,
    xTab: DoubleArray,
    fTab: DoubleArray,
    pdf: (Double) -> Double,
    zeroCase: (Rng, Double) -> Double,
): Double {
    while (true) {
        val bits = rng.nextU64()
        val i = (bits and 0xffuL).toInt()

        val u =
            if (symmetric) {
                (bits shr 12).intoFloatWithExponent(1) - 3.0
            } else {
                (bits shr 12).intoFloatWithExponent(0) - (1.0 - DOUBLE_EPSILON / 2.0)
            }
        val x = u * xTab[i]

        val testX = if (symmetric) abs(x) else x

        if (testX < xTab[i + 1]) {
            return x
        }
        if (i == 0) {
            return zeroCase(rng, u)
        }
        if (fTab[i + 1] + (fTab[i] - fTab[i + 1]) * rng.randomDouble() < pdf(x)) {
            return x
        }
    }
}
