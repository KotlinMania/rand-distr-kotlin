# rand-distr-kotlin in Kotlin

[![GitHub link](https://img.shields.io/badge/GitHub-KotlinMania%2Frand-distr-kotlin-blue.svg)](https://github.com/KotlinMania/rand-distr-kotlin)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.kotlinmania/rand-distr-kotlin)](https://central.sonatype.com/artifact/io.github.kotlinmania/rand-distr-kotlin)
[![Build status](https://img.shields.io/github/actions/workflow/status/KotlinMania/rand-distr-kotlin/ci.yml?branch=main)](https://github.com/KotlinMania/rand-distr-kotlin/actions)

This is a Kotlin Multiplatform line-by-line transliteration port of [`rand_distr`](https://github.com/rust-random/rand).

**Original Project:** This port is based on [`rand_distr`](https://github.com/rust-random/rand). All design credit and project intent belong to the upstream authors; this repository is a faithful port to Kotlin Multiplatform with no behavioural changes intended.

### Porting status

This is an **in-progress port**. The goal is feature parity with the upstream Rust crate while providing a native Kotlin Multiplatform API. Every Kotlin file carries a `// port-lint: source <path>` header naming its upstream Rust counterpart so the AST-distance tool can track provenance.

---

## Upstream README — `rand_distr`

> The text below is reproduced from [`https://github.com/rust-random/rand`](https://github.com/rust-random/rand). It is the upstream project's own description and remains under the upstream authors' authorship; links have been rewritten to absolute upstream URLs where appropriate so they continue to resolve from this repository.

# rand_distr

[![Test Status](https://github.com/rust-random/rand/actions/workflows/test.yml/badge.svg?event=push)](https://github.com/rust-random/rand/actions)
[![Latest version](https://img.shields.io/crates/v/rand_distr.svg)](https://crates.io/crates/rand_distr)
[![Book](https://img.shields.io/badge/book-master-yellow.svg)](https://rust-random.github.io/book/)
[![API](https://docs.rs/rand_distr/badge.svg)](https://docs.rs/rand_distr)

Implements a full suite of random number distribution sampling routines.

This crate is a superset of the [rand::distr] module, including support
for sampling from Beta, Binomial, Cauchy, ChiSquared, Dirichlet, Exponential,
FisherF, Gamma, Geometric, Hypergeometric, InverseGaussian, LogNormal, Normal,
Pareto, PERT, Poisson, StudentT, Triangular and Weibull distributions.  Sampling
from the unit ball, unit circle, unit disc and unit sphere surfaces is also
supported.

It is worth mentioning the [statrs] crate which provides similar functionality
along with various support functions, including PDF and CDF computation. In
contrast, this `rand_distr` crate focuses on sampling from distributions.

## Portability and libm

The floating point functions from `num_traits` and `libm` are used to support
`no_std` environments and ensure reproducibility. If the floating point
functions from `std` are preferred, which may provide better accuracy and
performance but may produce different random values, the `std_math` feature
can be enabled. (Note that any other crate depending on `num-traits` with the
`std` feature (default-enabled) will have the same effect.)

## Crate features

-   `std` (enabled by default): `rand_distr` implements the `Error` trait for
    its error types. Implies `alloc` and `rand/std`.
-   `alloc` (enabled by default): required for some distributions when not using
    `std` (in particular, `Dirichlet` and `WeightedAliasIndex`).
-   `std_math`: see above on portability and libm
-   `serde`: implement (de)seriaialization using `serde`

## Links

-   [API documentation (docs.rs)](https://docs.rs/rand_distr)
-   [Changelog](CHANGELOG.md)
-   [The Rand project](https://github.com/rust-random/rand)


[statrs]: https://github.com/boxtown/statrs
[rand::distr]: https://rust-random.github.io/rand/rand/distr/index.html

## Old Issues and Pull Requests

- All closed Issues and merged Pull requests from before 5.February.2025 (version 0.5.0) can be found in the [rand](https://github.com/rust-random/rand) repository where this crate used to be.

## License

`rand_distr` is distributed under the terms of both the MIT license and the
Apache License (Version 2.0).

See [LICENSE-APACHE](LICENSE-APACHE) and [LICENSE-MIT](LICENSE-MIT), and
[COPYRIGHT](COPYRIGHT) for details.

---

## About this Kotlin port

### Installation

```kotlin
dependencies {
    implementation("io.github.kotlinmania:rand-distr-kotlin:0.1.0")
}
```

### Building

```bash
./gradlew build
./gradlew test
```

### Targets

- macOS arm64
- Linux x64
- Windows mingw-x64
- iOS arm64 / simulator-arm64 (Swift export + XCFramework)
- JS (browser + Node.js)
- Wasm-JS (browser + Node.js)
- Android (API 24+)

### Porting guidelines

See [AGENTS.md](AGENTS.md) and [CLAUDE.md](CLAUDE.md) for translator discipline, port-lint header convention, and Rust → Kotlin idiom mapping.

### License

This Kotlin port is distributed under the same license terms as the upstream [`rand_distr`](https://github.com/rust-random/rand). See [LICENSE](LICENSE) (and any sibling `LICENSE-*` / `NOTICE` files mirrored from upstream) for the full text.

Original work copyrighted by the rand_distr authors.  
Kotlin port: Copyright (c) 2026 Sydney Renee and The Solace Project.

### Acknowledgments

Thanks to the [`rand_distr`](https://github.com/rust-random/rand) maintainers and contributors for the original Rust implementation. This port reproduces their work in Kotlin Multiplatform; bug reports about upstream design or behavior should go to the upstream repository.
