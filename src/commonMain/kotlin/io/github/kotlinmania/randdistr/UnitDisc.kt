// port-lint: source src/unit_disc.rs
package io.github.kotlinmania.randdistr

import io.github.kotlinmania.rand.Rng
import io.github.kotlinmania.rand.distr.Distribution
import io.github.kotlinmania.rand.distr.Uniform

/**
 * Samples uniformly from the unit disc in two dimensions.
 */
public object UnitDisc : Distribution<DoubleArray> {
    override fun sample(rng: Rng): DoubleArray {
        val uniform = Uniform.new(-1.0, 1.0).unwrap()
        var x1: Double
        var x2: Double
        while (true) {
            x1 = uniform.sample(rng)
            x2 = uniform.sample(rng)
            if (x1 * x1 + x2 * x2 <= 1.0) {
                break
            }
        }
        return doubleArrayOf(x1, x2)
    }
}
