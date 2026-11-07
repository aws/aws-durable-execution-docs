impl Plugin for ExamplePlugin {
    fn wrap_invocation(&self, info: &InvocationInfo, next: &mut dyn FnMut()) {
        tracing::info!("wrap invocation, arn: {}", info.execution_arn());
        next();
    }
}
