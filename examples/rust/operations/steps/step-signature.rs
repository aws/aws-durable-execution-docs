impl DurableContext {
    // ...
    pub fn step<O, F, Fut>(&self, f: F) -> StepBuilder<O, F, Fut>
    where
        F: FnOnce(StepContext) -> Fut + Clone + Send + 'static,
        Fut: Future<Output = Result<O, BoxError>> + Send + 'static,
        O: Send + 'static;
    // ...
}

impl<O, F, Fut, S> StepBuilder<O, F, Fut, S>
// ...
{
    pub fn future(self) -> DurableFuture<O>;
    // ...
    pub fn spawn(self) -> DurableFuture<O>;
    // ...
}

impl<O, F, Fut, S> IntoFuture for StepBuilder<O, F, Fut, S>
// ...
{
    type Output = Result<O, OperationError>;
    type IntoFuture = DurableFuture<O>;
    // ...
}
