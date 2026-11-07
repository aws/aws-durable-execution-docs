pub fn wait_strategy_fn<W>(
    self,
    strategy: W,
) -> WaitForConditionBuilder<S, F, Fut, SD, WithWaitStrategy<S>>
where
    W: Fn(S, u32) -> WaitDecision + Send + Sync + 'static;
// ...

pub enum WaitDecision {
    Complete,
    #[non_exhaustive]
    Continue {
        delay: Duration,
    },
    #[non_exhaustive]
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
