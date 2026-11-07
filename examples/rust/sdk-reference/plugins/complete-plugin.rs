use aws_durable_execution_sdk as durable;
use durable::plugin::{
    AttemptEndInfo, AttemptInfo, HookFuture, HookWork, InvocationEndInfo, InvocationInfo,
    OperationChangeInfo, OperationInfo, Plugin, PluginFactory, PropagationInput,
};
use durable::{OperationKind, OperationStatus, WireError};

struct ExamplePlugin;

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

    fn on_operation_change(&self, info: &OperationChangeInfo) -> HookWork {
        let ids: Vec<&str> = info
            .updated_operations()
            .iter()
            .map(OperationInfo::id)
            .collect();
        tracing::info!("operations changed, ids: {ids:?}");
        Ok(None)
    }

    fn on_attempt_start(&self, info: &AttemptInfo) -> HookWork {
        tracing::info!(
            "attempt {} start, attempt: {}",
            info.operation().name().unwrap_or_default(),
            info.attempt()
        );
        Ok(None)
    }

    fn on_attempt_end(&self, info: &AttemptEndInfo) -> HookWork {
        tracing::info!(
            "attempt {} end, attempt: {}, outcome: {}",
            info.operation().name().unwrap_or_default(),
            info.attempt(),
            info.outcome().wire_str()
        );
        Ok(None)
    }

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

    fn propagation_trace_id(
        &self,
        _input: &PropagationInput,
    ) -> Result<Option<String>, durable::BoxError> {
        Ok(None)
    }

    fn wrap_invocation(&self, info: &InvocationInfo, next: &mut dyn FnMut()) {
        tracing::info!("wrap invocation, arn: {}", info.execution_arn());
        tracing::info_span!("example_plugin", service = "orders").in_scope(next);
    }

    fn wrap_user_function(&self, info: &OperationInfo, next: &mut dyn FnMut()) {
        tracing::info!(
            "wrap user function {}, attempt: {:?}",
            info.name().unwrap_or_default(),
            info.attempt()
        );
        next();
    }
}

struct ExamplePluginFactory;

impl PluginFactory for ExamplePluginFactory {
    fn create_plugin(
        &self,
        _info: &InvocationInfo,
    ) -> Result<Option<Box<dyn Plugin>>, durable::BoxError> {
        Ok(Some(Box::new(ExamplePlugin)))
    }
}
