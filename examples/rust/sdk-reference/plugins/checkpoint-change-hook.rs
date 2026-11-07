impl Plugin for ExamplePlugin {
    fn on_operation_change(&self, info: &OperationChangeInfo) -> HookWork {
        let ids: Vec<&str> = info
            .updated_operations()
            .iter()
            .map(OperationInfo::id)
            .collect();
        tracing::info!("operations changed, ids: {ids:?}");
        Ok(None)
    }
}
