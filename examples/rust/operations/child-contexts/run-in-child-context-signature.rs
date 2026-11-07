pub struct ChildBuilder<O, F, Fut, S = JsonSerdes> {
    // ...
}

impl DurableContext {
    // ...

    pub fn run_in_child_context<O, F, Fut>(&self, f: F) -> ChildBuilder<O, F, Fut>
    where
        F: FnOnce(DurableContext) -> Fut + Send + 'static,
        Fut: Future<Output = Result<O, BoxError>> + Send + 'static,
        O: Send + 'static;

    // ...
}

impl<O, F, Fut, S> ChildBuilder<O, F, Fut, S>
where
    O: Send + 'static,
    F: FnOnce(DurableContext) -> Fut + Send + 'static,
    Fut: Future<Output = Result<O, BoxError>> + Send + 'static,
{
    // ...

    pub fn name(mut self, name: impl Into<String>) -> Self;

    // ...

    pub fn future(self) -> DurableFuture<O>
    where
        S: Serdes<O>;

    // ...

    pub fn spawn(self) -> DurableFuture<O>
    where
        S: Serdes<O>;

    // ...
}

impl<O, F, Fut, S> IntoFuture for ChildBuilder<O, F, Fut, S>
where
    O: Send + 'static,
    F: FnOnce(DurableContext) -> Fut + Send + 'static,
    Fut: Future<Output = Result<O, BoxError>> + Send + 'static,
    S: Serdes<O>,
{
    type Output = Result<O, OperationError>;
    // ...
}
