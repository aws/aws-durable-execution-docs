package main

import (
	"github.com/aws/aws-durable-execution-sdk-go/durable"
)

type Results struct {
	Result   string `json:"result"`
	Audit    string `json:"audit"`
	Critical string `json:"critical"`
}

func handler(ctx durable.Context, _ any) (Results, error) {
	// Default: 6 attempts, 5s initial delay, 60s max, 2x backoff, full jitter.
	result, err := durable.Step(ctx, "call-api",
		func(durable.StepContext) (string, error) { return callAPI() },
		durable.WithRetry(durable.ExponentialBackoff()))
	if err != nil {
		return Results{}, err
	}

	// Linear: 6 attempts, delays of 1s, 2s, 3s, 4s, 5s.
	audit, err := durable.Step(ctx, "audit-log",
		func(durable.StepContext) (string, error) { return writeAuditLog() },
		durable.WithRetry(durable.MustLinearBackoff(durable.LinearRetryConfig{})))
	if err != nil {
		return Results{}, err
	}

	// No retry: fail immediately on first error.
	critical, err := durable.Step(ctx, "charge-payment",
		func(durable.StepContext) (string, error) { return chargePayment() },
		durable.WithRetry(durable.NoRetry()))
	if err != nil {
		return Results{}, err
	}

	return Results{Result: result, Audit: audit, Critical: critical}, nil
}

func callAPI() (string, error)       { return "ok", nil }
func writeAuditLog() (string, error) { return "logged", nil }
func chargePayment() (string, error) { return "charged", nil }

func main() { durable.Start(handler) }
