package main

import (
	"errors"
	"time"

	"github.com/aws/aws-durable-execution-sdk-go/durable"
)

type DownstreamUnavailableError struct{ Message string }

func (e *DownstreamUnavailableError) Error() string { return e.Message }

func retryStrategy(a durable.RetryAttempt) durable.RetryDecision {
	if a.Attempt >= 10 {
		return durable.RetryDecision{}
	}
	var unavailable *DownstreamUnavailableError
	return durable.RetryDecision{
		Retry: true,
		Delay: 2 * time.Second,
		Pause: a.Attempt == 5 && errors.As(a.Err, &unavailable),
	}
}

func handler(ctx durable.Context, _ any) (string, error) {
	return durable.Step(ctx, "call-downstream",
		func(durable.StepContext) (string, error) {
			return callDownstream()
		},
		durable.WithRetry(retryStrategy),
	)
}

func callDownstream() (string, error) {
	return "ok", nil
}

func main() { durable.Start(handler) }
