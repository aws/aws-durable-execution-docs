impl Plugin for ExamplePlugin {
    fn wrap_user_function(&self, info: &OperationInfo, next: &mut dyn FnMut()) {
        if info.kind() == Some(OperationKind::Context) {
            tracing::info!("wrap child context {}", info.name().unwrap_or_default());
        }
        next();
    }
}
