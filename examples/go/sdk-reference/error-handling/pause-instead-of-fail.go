package main

import (
	"time"

	"github.com/aws/aws-durable-execution-sdk-go/durable"
)

const pauseFromAttempt = 3

func retryStrategy(a durable.RetryAttempt) durable.RetryDecision {
	return durable.RetryDecision{
		Retry: true,
		Delay: 5 * time.Second,
		Pause: a.Attempt >= pauseFromAttempt,
	}
}

func handler(ctx durable.Context, _ any) (string, error) {
	return durable.Step(ctx, "charge-payment",
		func(durable.StepContext) (string, error) {
			return chargePayment()
		},
		durable.WithRetry(retryStrategy),
	)
}

func chargePayment() (string, error) {
	return "ok", nil
}

func main() { durable.Start(handler) }
