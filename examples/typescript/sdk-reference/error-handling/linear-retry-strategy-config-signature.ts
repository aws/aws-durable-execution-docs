import { JitterStrategy, Duration } from "@aws/durable-execution-sdk-js";

export interface LinearRetryStrategyConfig {
  /** Default: 6 */
  maxAttempts?: number;
  /** Default: { seconds: 1 } */
  initialDelay?: Duration;
  /** Default: { seconds: 1 } */
  increment?: Duration;
  /** Default: { minutes: 5 } */
  maxDelay?: Duration;
  /** Default: JitterStrategy.FULL */
  jitter?: JitterStrategy;
  retryableErrors?: (string | RegExp)[];
  retryableErrorTypes?: (new () => Error)[];
}
