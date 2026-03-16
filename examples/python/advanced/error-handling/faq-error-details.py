try:
    result = context.step(operation())
except CallbackError as e:
    print(f"Callback failed: {e.callback_id}")
except NonDeterministicExecutionError as e:
    print(f"Non-deterministic step: {e.step_id}")
