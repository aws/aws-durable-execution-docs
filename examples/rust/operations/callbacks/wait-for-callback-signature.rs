impl DurableContext {
    // ...

    pub fn wait_for_callback<O>(
        &self,
        submitter: impl Submitter,
    ) -> WaitForCallbackBuilder<O, impl Submitter>
    where
        O: Send + 'static;

    // ...
}

pub trait Submitter:
    FnOnce(StepContext, String) -> <Self as Submitter>::Fut + Clone + Send + 'static
{
    type Fut: Future<Output = Result<(), BoxError>> + Send + 'static;
}

// ...

pub struct WaitForCallbackBuilder<O, F, S = JsonSerdes> {
    // ...
}

// ...

impl<O, F, S> WaitForCallbackBuilder<O, F, S>
where
    O: Send + 'static,
    F: Submitter,
{
    pub fn name(mut self, name: impl Into<String>) -> Self;

    // ...
}

impl<O, F, S> WaitForCallbackBuilder<O, F, S>
where
    O: Send + 'static,
    F: Submitter,
    S: Serdes<O>,
{
    pub fn future(self) -> DurableFuture<O>;

    // ...

    pub fn spawn(self) -> DurableFuture<O>;

    // ...
}

impl<O, F, S> IntoFuture for WaitForCallbackBuilder<O, F, S>
where
    O: Send + 'static,
    F: Submitter,
    S: Serdes<O>,
{
    type Output = Result<O, OperationError>;
    // ...
}
