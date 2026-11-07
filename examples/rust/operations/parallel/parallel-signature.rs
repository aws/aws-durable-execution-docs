pub fn parallel<Branches, O>(&self, branches: Branches) -> ParallelBuilder<O>
where
    Branches: IntoIterator<Item = Branch<O>>,
    O: Send + 'static,
...

impl<O: Send + 'static> Branch<O> {
    pub fn new<F, Fut>(name: impl Into<String>, factory: F) -> Self
    where
        F: FnOnce(DurableContext) -> Fut + Send + 'static,
        Fut: Future<Output = Result<O, BoxError>> + Send + 'static,
    ...
    pub fn unnamed<F, Fut>(factory: F) -> Self
    where
        F: FnOnce(DurableContext) -> Fut + Send + 'static,
        Fut: Future<Output = Result<O, BoxError>> + Send + 'static,
    ...
}

impl<O, IS, RS> IntoFuture for ParallelBuilder<O, IS, RS>
...
    type Output = Result<Vec<O>, OperationError>;
...

pub async fn await_batch(self) -> Result<BatchResult<O>, OperationError>
...
pub fn spawn_batch(self) -> DurableFuture<BatchResult<O>>
...
pub fn future(self) -> DurableFuture<Vec<O>>
...
pub fn spawn(self) -> DurableFuture<Vec<O>>
...

// Branches of different result types.
pub fn parallel_mixed(&self, branches: crate::BranchSet) -> ParallelMixedBuilder
...

pub fn add<T, F, Fut>(&mut self, name: impl Into<String>, factory: F) -> BranchHandle<T>
where
    T: serde::Serialize + serde::de::DeserializeOwned + Send + 'static,
    F: FnOnce(DurableContext) -> Fut + Send + 'static,
    Fut: Future<Output = Result<T, BoxError>> + Send + 'static,
...

pub fn summary(
    mut self,
    summary: impl Fn(&BatchResult<crate::future::BranchValue>) -> String + Send + Sync + 'static,
) -> Self
...
pub fn spawn(self) -> DurableFuture<CompletionReason>
...

impl<RS> IntoFuture for ParallelMixedBuilder<RS>
...
    type Output = Result<CompletionReason, OperationError>;
...

pub fn key(&self) -> BranchKey<T>
...
pub fn into_result(self) -> Result<T, OperationError>
...

pub fn value<T: 'static>(&self, key: &crate::BranchKey<T>) -> Option<&T>
