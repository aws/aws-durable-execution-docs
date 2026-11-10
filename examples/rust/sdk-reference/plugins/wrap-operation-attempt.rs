impl Plugin for ExamplePlugin {
    fn wrap_user_function(&self, info: &OperationInfo, next: &mut dyn FnMut()) {
        if let (Some(OperationKind::Step), Some(attempt)) = (info.kind(), info.attempt()) {
            tracing::info!(
                "wrap attempt {}, attempt: {attempt}",
                info.name().unwrap_or_default()
            );
        }
        next();
    }
}
