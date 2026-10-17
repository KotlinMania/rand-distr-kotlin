// port-lint: source src/unit_circle.rs
package io.github.kotlinmania.randdistr

import io.github.kotlinmania.rand.Rng
import io.github.kotlinmania.rand.distr.Distribution
import io.github.kotlinmania.rand.distr.Uniform

/**
 * Samples uniformly from the circumference of the unit circle in two dimensions.
 */
public object UnitCircle : Distribution<DoubleArray> {
    override fun sample(rng: Rng): DoubleArray {
        val uniform = Uniform.new(-1.0, 1.0).unwrap()
        var x1: Double
        var x2: Double
        var sum: Double
        while (true) {
            x1 = uniform.sample(rng)
            x2 = uniform.sample(rng)
            sum = x1 * x1 + x2 * x2
            if (sum < 1.0) {
                break
            }
        }
        val diff = x1 * x1 - x2 * x2
        return doubleArrayOf(diff / sum, 2.0 * x1 * x2 / sum)
    }
}
