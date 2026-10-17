package io.github.kotlinmania.randdistr

import io.github.kotlinmania.rand.Rng
import kotlin.math.abs
import kotlin.test.assertTrue

internal class Pcg32(
    seed: ULong,
    stream: ULong = 11634580027462260723uL,
) : Rng {
    private var state: ULong = 0uL
    private val increment: ULong = (stream shl 1) or 1uL

    init {
        step()
        state += seed
        step()
    }

    private fun step() {
        state = state * 6364136223846793005uL + increment
    }

    override fun nextU32(): UInt {
        val oldState = state
        step()
        val rot = (oldState shr 59).toInt()
        val xsh = (((oldState shr 18) xor oldState) shr 27).toUInt()
        return xsh.rotateRight(rot)
    }

    override fun nextU64(): ULong {
        val lo = nextU32().toULong()
        val hi = nextU32().toULong()
        return (hi shl 32) or lo
    }

    override fun fillBytes(dest: ByteArray) {
        var i = 0
        while (i < dest.size) {
            var v = nextU32()
            var j = 0
            while (j < 4 && i < dest.size) {
                dest[i] = (v and 0xffu).toByte()
                v = v shr 8
                i++
                j++
            }
        }
    }
}

internal class StepRng(
    private var v: ULong,
    private val a: ULong,
) : Rng {
    override fun nextU32(): UInt = nextU64().toUInt()

    override fun nextU64(): ULong {
        val result = v
        v += a
        return result
    }

    override fun fillBytes(dest: ByteArray) {
        var i = 0
        while (i < dest.size) {
            var word = nextU64()
            var j = 0
            while (j < 8 && i < dest.size) {
                dest[i] = (word and 0xffuL).toByte()
                word = word shr 8
                i++
                j++
            }
        }
    }
}

internal fun testRng(seed: ULong): Rng = Pcg32(seed)

internal fun assertAlmostEq(
    a: Double,
    b: Double,
    prec: Double,
) {
    val diff = abs(a - b)
    assertTrue(
        diff <= prec,
        "assertion failed: `abs(left - right) <= tolerance` (left: `$a`, right: `$b`, tolerance: `$prec`, diff: `$diff`)",
    )
}
