def on_operation_start(self, info: OperationStartInfo) -> None:
    print("operation start", info.operation_type, info.name)

def on_operation_end(self, info: OperationEndInfo) -> None:
    print("operation end", info.name, info.status, info.error)
