impl Plugin for ExamplePlugin {
    fn on_user_function_start(&self, info: &OperationInfo) -> HookWork {
        tracing::info!(
            "user function {} start, attempt: {:?}",
            info.name().unwrap_or_default(),
            info.attempt()
        );
        Ok(None)
    }

    fn on_user_function_end(
        &self,
        info: &OperationInfo,
        outcome: Result<(), &WireError>,
    ) -> HookWork {
        tracing::info!(
            "user function {} end, attempt: {:?}, outcome: {:?}",
            info.name().unwrap_or_default(),
            info.attempt(),
            outcome.map_err(ToString::to_string)
        );
        Ok(None)
    }
}
