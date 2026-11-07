impl Plugin for ExamplePlugin {
    fn on_operation_start(&self, info: &OperationInfo) -> HookWork {
        tracing::info!(
            "operation {} start, type: {:?}, replay: {}",
            info.name().unwrap_or_default(),
            info.kind().map(OperationKind::wire_str),
            info.is_replay()
        );
        Ok(None)
    }

    fn on_operation_end(&self, info: &OperationInfo) -> HookWork {
        tracing::info!(
            "operation {} end, status: {:?}, error: {:?}",
            info.name().unwrap_or_default(),
            info.status().map(OperationStatus::wire_str),
            info.error().map(ToString::to_string)
        );
        Ok(None)
    }
}
