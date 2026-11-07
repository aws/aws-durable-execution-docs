impl<O, F, Fut, S> StepBuilder<O, F, Fut, S>
where
    O: Send + 'static,
    F: FnOnce(StepContext) -> Fut + Clone + Send + 'static,
    Fut: Future<Output = Result<O, BoxError>> + Send + 'static,
{
    // ...
    pub fn retry_strategy<R>(mut self, strategy: R) -> Self
    where
        R: Fn(&crate::StepError, u32) -> crate::RetryDecision + Send + Sync + 'static;
    // ...
}

// ...

#[derive(Debug, Clone, PartialEq, Eq)]
#[non_exhaustive]
pub enum RetryDecision {
    Retry {
        delay: Duration,
    },
    Stop,
}
