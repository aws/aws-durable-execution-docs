pub type BoxError = Box<dyn std::error::Error + Send + Sync>;

// ...

pub async fn run<F, E, Fut, O>(handler: F) -> Result<(), lambda_runtime::Error>
where
    F: Fn(E, DurableContext) -> Fut + Send + Sync + 'static,
    E: for<'de> Deserialize<'de> + Send + 'static,
    Fut: Future<Output = Result<O, BoxError>> + Send,
    O: Serialize + Send + 'static;

// ...

pub async fn run_with_options<F, E, Fut, O>(
    handler: F,
    options: Options,
) -> Result<(), lambda_runtime::Error>
where
    F: Fn(E, DurableContext) -> Fut + Send + Sync + 'static,
    E: for<'de> Deserialize<'de> + Send + 'static,
    Fut: Future<Output = Result<O, BoxError>> + Send,
    O: Serialize + Send + 'static;

// ...

pub fn wrap<F, E, Fut, O>(
    handler: F,
    options: Options,
) -> impl Fn(lambda_runtime::LambdaEvent<InvocationPayload>) -> BoxedInvocationFuture + Send + Sync
where
    F: Fn(E, DurableContext) -> Fut + Send + Sync + 'static,
    E: for<'de> Deserialize<'de> + Send + 'static,
    Fut: Future<Output = Result<O, BoxError>> + Send,
    O: Serialize + Send + 'static;
