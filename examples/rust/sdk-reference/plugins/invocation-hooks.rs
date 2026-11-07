impl Plugin for ExamplePlugin {
    fn on_invocation_start<'a>(&'a self, info: &'a InvocationInfo) -> HookFuture<'a> {
        Box::pin(async move {
            tracing::info!(
                "invocation start, arn: {}, first invocation: {}",
                info.execution_arn(),
                info.is_first_invocation()
            );
            Ok(())
        })
    }

    fn on_invocation_end<'a>(&'a self, info: &'a InvocationEndInfo) -> HookFuture<'a> {
        Box::pin(async move {
            tracing::info!(
                "invocation end, arn: {}, status: {}",
                info.execution_arn(),
                info.status().wire_str()
            );
            Ok(())
        })
    }
}
