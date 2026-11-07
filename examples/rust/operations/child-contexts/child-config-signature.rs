impl<O, F, Fut, S> ChildBuilder<O, F, Fut, S>
where
    O: Send + 'static,
    F: FnOnce(DurableContext) -> Fut + Send + 'static,
    Fut: Future<Output = Result<O, BoxError>> + Send + 'static,
{
    // ...

    pub fn sub_type(mut self, sub_type: OperationSubType) -> Self;

    // ...

    pub fn serdes<S2>(self, serdes: S2) -> ChildBuilder<O, F, Fut, S2>
    where
        S2: Serdes<O>;

    // ...

    pub fn summary(mut self, summary: impl Fn(&O) -> String + Send + Sync + 'static) -> Self;

    // ...

    pub fn virtual_context(mut self) -> Self;

    // ...
}
