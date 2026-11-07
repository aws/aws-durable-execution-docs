impl Plugin for ExamplePlugin {
    fn wrap_invocation(&self, _info: &InvocationInfo, next: &mut dyn FnMut()) {
        tracing::info_span!("example_plugin", service = "orders").in_scope(next);
    }
}
