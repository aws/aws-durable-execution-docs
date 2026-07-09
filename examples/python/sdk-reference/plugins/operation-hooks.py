def on_operation_start(self, info: OperationStartInfo) -> None:
    print(f"operation {info.name} start, type: {info.operation_type}")

def on_operation_end(self, info: OperationEndInfo) -> None:
    print(f"operation {info.name} end, status: {info.status}, error: {info.error}")
