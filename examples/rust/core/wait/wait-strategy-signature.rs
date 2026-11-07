pub fn wait_strategy_fn<W>(mut self, strategy: W) -> Self
where
    W: Fn(S, u32) -> WaitDecision + Send + Sync + 'static;
// ...

pub enum WaitDecision {
    Complete,
    Continue {
        delay: Duration,
    },
    Exhausted {
        reason: String,
    },
}

impl WaitDecision {
    pub fn complete() -> Self;
    // ...
    pub fn continue_with(delay: Duration) -> Self;
    // ...
    pub fn exhausted(reason: impl Into<String>) -> Self;
    // ...
}
