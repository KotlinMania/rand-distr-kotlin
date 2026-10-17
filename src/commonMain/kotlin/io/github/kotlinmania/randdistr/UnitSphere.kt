// port-lint: source src/unit_sphere.rs
package io.github.kotlinmania.randdistr

import io.github.kotlinmania.rand.Rng
import io.github.kotlinmania.rand.distr.Distribution
import io.github.kotlinmania.rand.distr.Uniform
import kotlin.math.sqrt

/**
 * Samples uniformly from the surface of the unit sphere in three dimensions.
 */
public object UnitSphere : Distribution<DoubleArray> {
    override fun sample(rng: Rng): DoubleArray {
        val uniform = Uniform.new(-1.0, 1.0).unwrap()
        while (true) {
            val x1 = uniform.sample(rng)
            val x2 = uniform.sample(rng)
            val sum = x1 * x1 + x2 * x2
            if (sum >= 1.0) {
                continue
            }
            val factor = 2.0 * sqrt(1.0 - sum)
            return doubleArrayOf(
                x1 * factor,
                x2 * factor,
                1.0 - 2.0 * sum,
            )
        }
    }
}
