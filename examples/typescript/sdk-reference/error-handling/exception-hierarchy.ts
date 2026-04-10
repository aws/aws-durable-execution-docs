import {
  DurableOperationError,
  StepError,
  CallbackError,
  CallbackTimeoutError,
  CallbackSubmitterError,
  InvokeError,
  ChildContextError,
  WaitForConditionError,
  StepInterruptedError,
} from "@aws/durable-execution-sdk-js";

// DurableOperationError
//   StepError              — step failed after retries exhausted
//   CallbackError          — callback operation failed
//   CallbackTimeoutError   — callback timed out
//   CallbackSubmitterError — callback submitter failed
//   InvokeError            — invoke operation failed
//   ChildContextError      — child context failed
//   WaitForConditionError  — wait-for-condition failed
//
// StepInterruptedError     — at-most-once step interrupted before checkpoint
//   (not a DurableOperationError; thrown directly)

export {
  DurableOperationError,
  StepError,
  CallbackError,
  CallbackTimeoutError,
  CallbackSubmitterError,
  InvokeError,
  ChildContextError,
  WaitForConditionError,
  StepInterruptedError,
};
