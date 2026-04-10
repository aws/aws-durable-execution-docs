from aws_durable_execution_sdk_python.exceptions import ExecutionError, InvocationError

try:
    # Your code here
    pass
except (ExecutionError, InvocationError) as e:
    # Access termination reason from unrecoverable errors
    print(f"Execution terminated: {e.termination_reason}")
