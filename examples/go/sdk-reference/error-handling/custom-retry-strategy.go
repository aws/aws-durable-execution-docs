package main

import (
	"time"

	"github.com/aws/aws-durable-execution-sdk-go/durable"
)

// A RetryStrategy is a plain function: func(RetryAttempt) RetryDecision.
// Attempt is the 1-based number of the attempt that just failed.
func customRetryStrategy(a durable.RetryAttempt) durable.RetryDecision {
	if a.Attempt >= 4 {
		return durable.RetryDecision{}
	}
	// Fixed 2-second delay regardless of attempt number.
	return durable.RetryDecision{Retry: true, Delay: 2 * time.Second}
}

func handler(ctx durable.Context, _ any) (string, error) {
	return durable.Step(ctx, "call-api",
		func(durable.StepContext) (string, error) {
			return callAPI()
		},
		durable.WithRetry(customRetryStrategy),
	)
}

func callAPI() (string, error) {
	return "ok", nil
}

func main() { durable.Start(handler) }
