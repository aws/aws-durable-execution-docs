pub trait Serdes<T>: Send + Sync + 'static {
    fn serialize(
        &self,
        value: T,
        context: SerdesContext,
    ) -> impl Future<Output = Result<String, BoxError>> + Send;

    fn deserialize(
        &self,
        wire: String,
        context: SerdesContext,
    ) -> impl Future<Output = Result<T, BoxError>> + Send;
}

// ...

impl SerdesContext {
    // ...
    pub fn operation_id(&self) -> &str;
    // ...
    pub fn durable_execution_arn(&self) -> &str;
    // ...
    pub fn origin(&self) -> PayloadOrigin;
    // ...
}

// ...

#[derive(Debug, Clone, Copy, PartialEq, Eq)]
#[non_exhaustive]
pub enum PayloadOrigin {
    SdkRecord,
    External,
}
