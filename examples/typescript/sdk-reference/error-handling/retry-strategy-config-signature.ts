import {
  RetryStrategyConfig,
  JitterStrategy,
  Duration,
} from "@aws/durable-execution-sdk-js";

// RetryStrategyConfig shape:
// {
//   maxAttempts?: number          // default: 3
//   initialDelay?: Duration       // default: { seconds: 5 }
//   maxDelay?: Duration           // default: { minutes: 5 }
//   backoffRate?: number          // default: 2
//   jitter?: JitterStrategy       // default: JitterStrategy.FULL
//   retryableErrors?: (string | RegExp)[]
//   retryableErrorTypes?: (new () => Error)[]
// }
