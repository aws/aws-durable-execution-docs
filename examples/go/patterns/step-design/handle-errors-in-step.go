package main

import (
	"time"

	"github.com/aws/aws-durable-execution-sdk-go/durable"
)

type Event struct {
	ID string `json:"id"`
}

type TransientAPIError struct{ Message string }

func (e *TransientAPIError) Error() string { return e.Message }

type RateLimitError struct{ Message string }

func (e *RateLimitError) Error() string { return e.Message }

func callAPI(id string) (string, error) { return "result-" + id, nil }

func handler(ctx durable.Context, event Event) (string, error) {
	// Retry only transient errors. Anything else fails the step immediately.
	retry := durable.MustNewRetryStrategy(durable.RetryConfig{
		MaxAttempts:  5,
		InitialDelay: 2 * time.Second,
		RetryableErrors: []durable.ErrorMatcher{
			durable.ErrorAs[*TransientAPIError](),
			durable.ErrorAs[*RateLimitError](),
		},
	})

	return durable.Step(ctx, "call-api", func(durable.StepContext) (string, error) {
		return callAPI(event.ID)
	}, durable.WithRetry(retry))
}

func main() {
	durable.Start(handler)
}
