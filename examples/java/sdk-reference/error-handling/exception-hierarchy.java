import software.amazon.lambda.durable.exception.DurableExecutionException;
import software.amazon.lambda.durable.exception.DurableOperationException;
import software.amazon.lambda.durable.exception.UnrecoverableDurableExecutionException;
import software.amazon.lambda.durable.exception.StepException;
import software.amazon.lambda.durable.exception.StepFailedException;
import software.amazon.lambda.durable.exception.StepInterruptedException;
import software.amazon.lambda.durable.exception.CallbackException;
import software.amazon.lambda.durable.exception.CallbackFailedException;
import software.amazon.lambda.durable.exception.SerDesException;
import software.amazon.lambda.durable.exception.IllegalDurableOperationException;
import software.amazon.lambda.durable.exception.NonDeterministicExecutionException;

// DurableExecutionException
//   UnrecoverableDurableExecutionException — execution terminated immediately
//     IllegalDurableOperationException     — illegal SDK operation detected
//     NonDeterministicExecutionException   — non-deterministic replay detected
//   DurableOperationException              — operation-level failure with details
//     StepException
//       StepFailedException                — step failed after retries exhausted
//       StepInterruptedException           — at-most-once step interrupted
//     CallbackException
//       CallbackFailedException            — callback failed with error from external system
//   SerDesException                        — serialization or deserialization failed
