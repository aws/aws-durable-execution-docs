def on_operation_change(self, info: OperationChangeInfo) -> None:
    print("operations changed", "ids:", list(info.updated_operations.keys()))
