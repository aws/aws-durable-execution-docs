impl DurableContext {
    // ...
    pub fn wait_for_condition<S, F, Fut>(
        &self,
        check: F,
        initial_state: S,
    ) -> WaitForConditionBuilder<S, F, Fut>
    where
        F: Fn(StepContext, S) -> Fut + Send + Sync + 'static,
        Fut: Future<Output = Result<S, BoxError>> + Send + 'static,
        S: Clone + Send + Sync + 'static;
    // ...
}
// ...

pub struct WaitForConditionBuilder<S, F, Fut, SD = JsonSerdes> {
    // ...
}

impl<S, F, Fut, SD> WaitForConditionBuilder<S, F, Fut, SD>
where
    S: Clone + Send + Sync + 'static,
    F: Fn(StepContext, S) -> Fut + Send + Sync + 'static,
    Fut: Future<Output = Result<S, BoxError>> + Send + 'static,
{
    pub fn name(mut self, name: impl Into<String>) -> Self;
    // ...
    pub fn wait_strategy(mut self, strategy: WaitStrategy<S>) -> Self;
    // ...
    pub fn wait_strategy_fn<W>(mut self, strategy: W) -> Self
    where
        W: Fn(S, u32) -> WaitDecision + Send + Sync + 'static;
    // ...
    pub fn serdes<SD2>(self, serdes: SD2) -> WaitForConditionBuilder<S, F, Fut, SD2>
    where
        SD2: Serdes<S>;
    // ...
    pub fn future(self) -> DurableFuture<S>
    where
        SD: Serdes<S>;
    // ...
    pub fn spawn(self) -> DurableFuture<S>
    where
        SD: Serdes<S>;
    // ...
}

impl<S, F, Fut, SD> IntoFuture for WaitForConditionBuilder<S, F, Fut, SD>
// ...
{
    type Output = Result<S, OperationError>;
    // ...
}
