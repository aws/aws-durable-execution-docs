def on_invocation_start(self, info: InvocationStartInfo) -> None:
    print(f"invocation start, arn: {info.execution_arn}, first invocation: {info.is_first_invocation}")

def on_invocation_end(self, info: InvocationEndInfo) -> None:
    print(f"invocation end, arn: {info.execution_arn}, status: {info.status}")
