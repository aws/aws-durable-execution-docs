def on_user_function_start(self, info: UserFunctionStartInfo) -> None:
    print(f"user function {info.name} start, attempt: {info.attempt}")

def on_user_function_end(self, info: UserFunctionEndInfo) -> None:
    print(f"user function {info.name} end, attempt: {info.attempt}, outcome: {info.outcome}")
