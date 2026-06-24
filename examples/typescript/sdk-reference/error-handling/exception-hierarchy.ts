import {
  DurableOperationError,
  StepError,
  CallbackError,
  CallbackExternalError,
  CallbackTimeoutError,
  CallbackSubmitterError,
  InvokeError,
  ChildContextError,
  PromiseCombinatorError,
  WaitForConditionError,
  StepInterruptedError,
} from "@aws/durable-execution-sdk-js";

// DurableOperationError
//   StepError:               step failed after retries exhausted
//   CallbackError:           parent class for callback failures
//     CallbackExternalError:   external entity reported failure
//     CallbackTimeoutError:    callback timed out
//     CallbackSubmitterError:  submitter function failed
//   InvokeError:             invoke operation failed
//   ChildContextError:       child context failed
//   PromiseCombinatorError:  context.promise.{all,allSettled,any,race} failed
//   WaitForConditionError:   wait-for-condition failed
//
// StepInterruptedError: internal sentinel. The SDK passes it to your
//   retryStrategy(error, attempt) callback when Lambda interrupts an
//   at-most-once step. From context.step(...) the SDK throws a StepError
//   whose cause.name === "StepInterruptedError".

export {
  DurableOperationError,
  StepError,
  CallbackError,
  CallbackExternalError,
  CallbackTimeoutError,
  CallbackSubmitterError,
  InvokeError,
  ChildContextError,
  PromiseCombinatorError,
  WaitForConditionError,
  StepInterruptedError,
};
