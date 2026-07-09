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
        print(f"invocation start, arn: {info.execution_arn}, first invocation: {info.is_first_invocation}")

    def on_invocation_end(self, info: InvocationEndInfo) -> None:
        print(f"invocation end, arn: {info.execution_arn}, status: {info.status}")

    def on_operation_start(self, info: OperationStartInfo) -> None:
        print(f"operation {info.name} start, type: {info.operation_type}")

    def on_operation_end(self, info: OperationEndInfo) -> None:
        print(f"operation {info.name} end, status: {info.status}, error: {info.error}")

    def on_user_function_start(self, info: UserFunctionStartInfo) -> None:
        print(f"user function {info.name} start, attempt: {info.attempt}")

    def on_user_function_end(self, info: UserFunctionEndInfo) -> None:
        print(f"user function {info.name} end, attempt: {info.attempt}, outcome: {info.outcome}")
