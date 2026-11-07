#[derive(Debug, Clone, Default)]
#[non_exhaustive]
pub struct ReplayFilterLayer;

impl ReplayFilterLayer {
    pub fn new() -> Self;
    // ...
}

impl<S> subscriber::layer::Filter<S> for ReplayFilterLayer
where
    S: tracing::Subscriber + for<'lookup> subscriber::registry::LookupSpan<'lookup>,
{
    // ...
}
