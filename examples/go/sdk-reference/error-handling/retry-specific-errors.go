package main

import (
	"time"

	"github.com/aws/aws-durable-execution-sdk-go/durable"
)

type RateLimitError struct{ Message string }

func (e *RateLimitError) Error() string { return e.Message }

type ServiceUnavailableError struct{ Message string }

func (e *ServiceUnavailableError) Error() string { return e.Message }

var retryStrategy = durable.MustNewRetryStrategy(durable.RetryConfig{
	MaxAttempts:  5,
	InitialDelay: 2 * time.Second,
	// Only retry these specific error types; all other errors fail immediately.
	RetryableErrors: []durable.ErrorMatcher{
		durable.ErrorAs[*RateLimitError](),
		durable.ErrorAs[*ServiceUnavailableError](),
	},
})

func handler(ctx durable.Context, _ any) (string, error) {
	return durable.Step(ctx, "call-api",
		func(durable.StepContext) (string, error) {
			// Returns *RateLimitError or *ServiceUnavailableError on transient failures.
			return callAPI()
		},
		durable.WithRetry(retryStrategy),
	)
}

func callAPI() (string, error) {
	return "ok", nil
}

func main() { durable.Start(handler) }
