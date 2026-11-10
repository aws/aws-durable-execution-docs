#[derive(Debug, Clone, Copy, PartialEq)]
#[non_exhaustive]
pub enum Backoff {
    #[non_exhaustive]
    Exponential {
        rate: f64,
    },
    #[non_exhaustive]
    Linear {
        increment: Duration,
    },
}

impl Backoff {
    #[must_use]
    pub fn exponential(rate: f64) -> Self;
    // ...
    #[must_use]
    pub fn linear(increment: Duration) -> Self;
    // ...
}
