def on_user_function_start(self, info: UserFunctionStartInfo) -> None:
    print("user function start", info.name, info.attempt)

def on_user_function_end(self, info: UserFunctionEndInfo) -> None:
    print("user function end", info.name, info.attempt, info.outcome)
