using Amazon.Lambda.DurableExecution;

// DurableExecutionException                — base class for all SDK exceptions
//   NonDeterministicExecutionException     — non-deterministic replay detected
//   StepException                          — step failed after retries exhausted
//     StepInterruptedException             — at-most-once step interrupted mid-attempt
//   ChildContextException                  — child context's user function failed
//   InvokeException                        — chained invoke failed
//     InvokeFailedException                — invoked function ran and threw (FAILED)
//     InvokeTimedOutException              — invoked function reached TIMED_OUT
//     InvokeStoppedException               — invocation stopped administratively
//   CallbackException                      — parent class for callback failures
//     CallbackFailedException              — external system reported a failure
//     CallbackTimeoutException             — callback (or heartbeat) timeout elapsed
//     CallbackSubmitterException           — submitter step failed after retries
//   ParallelException                      — parallel exceeded failure tolerance
//   MapException                           — map exceeded failure tolerance
//   WaitForConditionException              — wait-for-condition hit max attempts
//
// OperationCanceledException (System) — NOT a DurableExecutionException. Thrown
//   when the linked CancellationToken trips (workflow shutdown or cooperative
//   short-circuit). Treated as cancellation, not a step failure.
