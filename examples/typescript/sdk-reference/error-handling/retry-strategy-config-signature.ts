import { JitterStrategy, Duration } from "@aws/durable-execution-sdk-js";

export interface RetryStrategyConfig {
  /** Default: 3 */
  maxAttempts?: number;
  /** Default: { seconds: 5 } */
  initialDelay?: Duration;
  /** Default: { minutes: 5 } */
  maxDelay?: Duration;
  /** Default: 2 */
  backoffRate?: number;
  /** Default: JitterStrategy.FULL */
  jitter?: JitterStrategy;
  retryableErrors?: (string | RegExp)[];
  retryableErrorTypes?: (new () => Error)[];
  /** Default: undefined (never pauses) */
  pauseEvery?: number;
}
