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

pub struct WaitForConditionBuilder<S, F, Fut, SD = JsonSerdes, WS = NoWaitStrategy> {
    // ...
}

impl<S, F, Fut, SD, WS> WaitForConditionBuilder<S, F, Fut, SD, WS>
where
    S: Clone + Send + Sync + 'static,
    F: Fn(StepContext, S) -> Fut + Send + Sync + 'static,
    Fut: Future<Output = Result<S, BoxError>> + Send + 'static,
{
    pub fn name(mut self, name: impl Into<String>) -> Self;
    // ...
    pub fn wait_strategy(
        self,
        strategy: WaitStrategy<S>,
    ) -> WaitForConditionBuilder<S, F, Fut, SD, WithWaitStrategy<S>>;
    // ...
    pub fn wait_strategy_fn<W>(
        self,
        strategy: W,
    ) -> WaitForConditionBuilder<S, F, Fut, SD, WithWaitStrategy<S>>
    where
        W: Fn(S, u32) -> WaitDecision + Send + Sync + 'static;
    // ...
    pub fn serdes<SD2>(self, serdes: SD2) -> WaitForConditionBuilder<S, F, Fut, SD2, WS>
    where
        SD2: Serdes<S>;
    // ...
}

impl<S, F, Fut, SD> WaitForConditionBuilder<S, F, Fut, SD, WithWaitStrategy<S>>
where
    S: Clone + Send + Sync + 'static,
    F: Fn(StepContext, S) -> Fut + Send + Sync + 'static,
    Fut: Future<Output = Result<S, BoxError>> + Send + 'static,
    SD: Serdes<S>,
{
    pub fn future(self) -> DurableFuture<S>;
    // ...
    pub fn spawn(self) -> DurableFuture<S>;
    // ...
}

impl<S, F, Fut, SD> IntoFuture for WaitForConditionBuilder<S, F, Fut, SD, WithWaitStrategy<S>>
// ...
{
    type Output = Result<S, OperationError>;
    // ...
}
