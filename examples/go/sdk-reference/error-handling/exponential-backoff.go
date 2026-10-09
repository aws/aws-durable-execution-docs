package main

import (
	"time"

	"github.com/aws/aws-durable-execution-sdk-go/durable"
)

var retryStrategy = durable.MustNewRetryStrategy(durable.RetryConfig{
	MaxAttempts:  5,
	InitialDelay: 2 * time.Second,
	MaxDelay:     1 * time.Minute,
	BackoffRate:  2,
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
