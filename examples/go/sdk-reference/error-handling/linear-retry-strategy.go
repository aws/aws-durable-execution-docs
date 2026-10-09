package main

import (
	"time"

	"github.com/aws/aws-durable-execution-sdk-go/durable"
)

var retryStrategy = durable.MustLinearBackoff(durable.LinearRetryConfig{
	MaxAttempts:  5,
	InitialDelay: 2 * time.Second,
	Increment:    3 * time.Second,
	MaxDelay:     30 * time.Second,
	// LinearBackoff defaults to JitterNone, so set full jitter explicitly.
	Jitter: durable.JitterFull,
})

func handler(ctx durable.Context, _ any) (string, error) {
	return durable.Step(ctx, "call-external-api",
		func(durable.StepContext) (string, error) {
			return callExternalAPI()
		},
		durable.WithRetry(retryStrategy),
	)
}

func callExternalAPI() (string, error) {
	return "ok", nil
}

func main() { durable.Start(handler) }
