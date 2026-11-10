pub use aws_durable_execution_sdk::{
    CallbackError, CallbackErrorKind, ChildContextError, ChildContextErrorKind, CombinatorError,
    CombinatorErrorKind, InvokeError, InvokeErrorKind, NonDeterministicExecutionError,
    NonDeterministicExecutionErrorKind, OperationError, OperationErrorKind, ReplayedFailure,
    StepError, StepErrorKind, WaitError, WaitErrorKind, WaitForConditionError,
    WaitForConditionErrorKind, WireError,
};

// OperationError                   every operation failure
//   Step(StepError)                  step failed
//     ExecutionFailed                  the body failed and the retry strategy stopped
//     Interrupted                      an at-most-once attempt recorded no outcome
//     SerializationFailed              result serialization or deserialization failed
//   Wait(WaitError)                  wait record could not be read
//   Invoke(InvokeError)              invoke operation failed
//   Callback(CallbackError)          every callback failure
//     ExternalFailure                  external system reported failure
//     TimedOut                         callback timed out
//     HeartbeatTimedOut                no heartbeat arrived in time
//     SubmitterFailed                  submitter function failed
//     DeserializationFailed            payload did not decode
//     Internal                         SDK or service failure
//   WaitForCondition(WaitForConditionError)
//                                    wait-for-condition failed
//   ChildContext(ChildContextError)  child context, map, or parallel failed
//   Combinator(CombinatorError)      join_all, try_join_all, select_ok, or race failed
//   NonDeterministicExecution(NonDeterministicExecutionError)
//                                    operation mismatch or unrecognized status
//
// WireError        the recorded type, message, data, and stack trace (OperationError::wire())
// ReplayedFailure  the source() of a failure that replay rebuilt from its record
