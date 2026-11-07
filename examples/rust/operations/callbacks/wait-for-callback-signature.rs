impl DurableContext {
    // ...

    pub fn wait_for_callback<O, F, Fut>(&self, submitter: F) -> WaitForCallbackBuilder<O, F, Fut>
    where
        F: FnOnce(StepContext, String) -> Fut + Clone + Send + 'static,
        Fut: Future<Output = Result<(), BoxError>> + Send + 'static,
        O: Send + 'static;

    // ...
}

pub struct WaitForCallbackBuilder<O, F, Fut, S = JsonSerdes> {
    // ...
}

impl<O, F, Fut, S> WaitForCallbackBuilder<O, F, Fut, S>
where
    O: Send + 'static,
    F: FnOnce(StepContext, String) -> Fut + Clone + Send + 'static,
    Fut: Future<Output = Result<(), BoxError>> + Send + 'static,
{
    pub fn name(mut self, name: impl Into<String>) -> Self;

    // ...
}

impl<O, F, Fut, S> WaitForCallbackBuilder<O, F, Fut, S>
where
    O: Send + 'static,
    F: FnOnce(StepContext, String) -> Fut + Clone + Send + 'static,
    Fut: Future<Output = Result<(), BoxError>> + Send + 'static,
    S: Serdes<O>,
{
    pub fn future(self) -> DurableFuture<O>;

    // ...

    pub fn spawn(self) -> DurableFuture<O>;

    // ...
}

impl<O, F, Fut, S> IntoFuture for WaitForCallbackBuilder<O, F, Fut, S>
where
    O: Send + 'static,
    F: FnOnce(StepContext, String) -> Fut + Clone + Send + 'static,
    Fut: Future<Output = Result<(), BoxError>> + Send + 'static,
    S: Serdes<O>,
{
    type Output = Result<O, OperationError>;
    // ...
}
