pub struct CreateCallbackBuilder<O, S = JsonSerdes> {
    // ...
}

impl DurableContext {
    // ...

    pub fn create_callback<O>(&self) -> CreateCallbackBuilder<O>
    where
        O: Send + 'static;

    // ...
}

impl<O: Send + 'static, S> CreateCallbackBuilder<O, S> {
    pub fn name(mut self, name: impl Into<String>) -> Self;

    // ...

    pub fn future(self) -> DurableFuture<Callback<O>>
    where
        S: Serdes<O>;

    // ...

    pub fn spawn(self) -> DurableFuture<Callback<O>>
    where
        S: Serdes<O>;

    // ...
}

impl<O, S> IntoFuture for CreateCallbackBuilder<O, S>
where
    O: Send + 'static,
    S: Serdes<O>,
{
    type Output = Result<Callback<O>, OperationError>;
    // ...
}
