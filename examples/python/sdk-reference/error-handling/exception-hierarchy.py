from aws_durable_execution_sdk_python.exceptions import (
    DurableExecutionsError,
    UnrecoverableError,
    ExecutionError,
    InvocationError,
    CallbackError,
    ValidationError,
    SerDesError,
    StepInterruptedError,
    CallableRuntimeError,
    UserlandError,
)

# DurableExecutionsError
#   UnrecoverableError
#     ExecutionError         — fails execution without retry
#       CallbackError        — callback handling failed
#     InvocationError        — triggers Lambda retry
#       StepInterruptedError — at-most-once step interrupted
#   ValidationError          — invalid arguments to SDK operations
#   SerDesError              — serialization or deserialization failed
#   UserlandError
#     CallableRuntimeError   — wraps exceptions thrown inside step functions
