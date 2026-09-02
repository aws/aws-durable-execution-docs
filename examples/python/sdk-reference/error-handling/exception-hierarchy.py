from aws_durable_execution_sdk_python import (
    BatchCompletionError,
    CallbackError,
    CallbackExternalError,
    CallbackSubmitterError,
    CallbackTimeoutError,
    ChildContextError,
    DurableExecutionsError,
    DurableOperationError,
    ExecutionError,
    InvocationError,
    InvokeError,
    RetryableSerDesError,
    SerDesError,
    StepError,
    ValidationError,
    WaitForConditionError,
)

# DurableExecutionsError                 — base for all SDK exceptions
#   ValidationError                      — invalid arguments to SDK operations
#   SerDesError                          — permanent serialize/deserialize failure
#   UnrecoverableError
#     ExecutionError                     — fails execution without retry
#       NonDeterministicExecutionError   — replay diverged from recorded history
#     InvocationError                    — triggers Lambda retry
#       StepInterruptedError             — at-most-once step interrupted
#       RetryableSerDesError             — transient serdes failure, retries
#   DurableOperationError                — base for per-operation failures
#     StepError                          — step exhausted its retries
#     InvokeError                        — chained invoke failed
#     ChildContextError                  — child context failed
#     WaitForConditionError              — wait_for_condition exhausted attempts
#     BatchCompletionError               — custom completion predicate failed the batch
#     CallbackError                      — base for callback failures
#       CallbackExternalError            — external system reported failure
#       CallbackTimeoutError             — callback or heartbeat timed out
#       CallbackSubmitterError           — submitter function raised
