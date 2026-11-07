pub fn init_default_subscriber() -> Result<(), BoxError>;

// ...

pub fn init_default_subscriber_with_writer<W>(writer: W) -> Result<(), BoxError>
where
    W: for<'w> lambda_runtime::tracing::subscriber::fmt::MakeWriter<'w> + Send + Sync + 'static;
