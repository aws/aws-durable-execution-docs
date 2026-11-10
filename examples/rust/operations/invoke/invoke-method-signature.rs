impl DurableContext {
    // ...
    pub fn invoke<O>(
        &self,
        function_id: impl Into<String>,
        input: impl Serialize + Send + 'static,
    ) -> InvokeBuilder<O, impl Serialize + Send + 'static>
    where
        O: Send + 'static;
    // ...
}
// ...

pub struct InvokeBuilder<O, I, PS = JsonSerdes, RS = JsonSerdes> {
    // ...
}

impl<O, I, PS, RS> InvokeBuilder<O, I, PS, RS>
where
    O: Send + 'static,
    I: Serialize + Send + 'static,
    PS: PayloadSerializer,
    RS: Serdes<O>,
{
    pub fn future(self) -> DurableFuture<O>;
    // ...
    pub fn spawn(self) -> DurableFuture<O>;
    // ...
}

impl<O, I, PS, RS> IntoFuture for InvokeBuilder<O, I, PS, RS>
// ...
{
    type Output = Result<O, OperationError>;
    type IntoFuture = DurableFuture<O>;
    // ...
}
