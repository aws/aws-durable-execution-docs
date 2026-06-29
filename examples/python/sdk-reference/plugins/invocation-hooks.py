def on_invocation_start(self, info: InvocationStartInfo) -> None:
    print("invocation start", info.execution_arn, info.is_first_invocation)

def on_invocation_end(self, info: InvocationEndInfo) -> None:
    print("invocation end", info.status)
