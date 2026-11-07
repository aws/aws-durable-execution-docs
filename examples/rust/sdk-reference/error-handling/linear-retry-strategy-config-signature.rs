#[derive(Debug, Clone, Copy, PartialEq)]
#[non_exhaustive]
pub enum Backoff {
    Exponential {
        rate: f64,
    },
    Linear {
        increment: Duration,
    },
}
