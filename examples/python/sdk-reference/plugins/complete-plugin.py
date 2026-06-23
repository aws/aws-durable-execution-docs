from aws_durable_execution_sdk_python.plugin import (
    DurableInstrumentationPlugin,
    InvocationStartInfo,
    InvocationEndInfo,
    OperationStartInfo,
    OperationEndInfo,
    UserFunctionStartInfo,
    UserFunctionEndInfo,
)


class ExamplePlugin(DurableInstrumentationPlugin):
    def on_invocation_start(self, info: InvocationStartInfo) -> None:
        print("invocation start", info.execution_arn, info.is_first_invocation)

    def on_invocation_end(self, info: InvocationEndInfo) -> None:
        print("invocation end", info.status)

    def on_operation_start(self, info: OperationStartInfo) -> None:
        print("operation start", info.operation_type, info.name)

    def on_operation_end(self, info: OperationEndInfo) -> None:
        print("operation end", info.name, info.status, info.error)

    def on_user_function_start(self, info: UserFunctionStartInfo) -> None:
        print("user function start", info.name, info.attempt)

    def on_user_function_end(self, info: UserFunctionEndInfo) -> None:
        print("user function end", info.name, info.attempt, info.outcome)
