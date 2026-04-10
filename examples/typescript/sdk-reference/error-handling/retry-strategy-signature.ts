import { RetryDecision } from "@aws/durable-execution-sdk-js";

// (error: Error, attemptCount: number) => RetryDecision
// attemptCount is one-indexed: 1 on the first retry, 2 on the second, etc.

type RetryStrategy = (error: Error, attemptCount: number) => RetryDecision;
