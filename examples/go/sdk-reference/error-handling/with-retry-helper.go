package main

import (
	"fmt"
	"time"

	"github.com/aws/aws-durable-execution-sdk-go/durable"
)

type OrderEvent struct {
	OrderID string `json:"orderId"`
}

type PaymentRequest struct {
	OrderID string `json:"orderId"`
}

var retryStrategy = durable.MustNewRetryStrategy(durable.RetryConfig{
	MaxAttempts:  3,
	InitialDelay: 2 * time.Second,
	BackoffRate:  2,
})

func handler(ctx durable.Context, event OrderEvent) (string, error) {
	// Invoke does not accept a retry strategy, so wrap it with Retry to
	// apply backoff between failed attempts.
	return durable.Retry(ctx, "charge-payment",
		func(c durable.Context, attempt int) (string, error) {
			return durable.Invoke[string](c,
				fmt.Sprintf("charge-%d", attempt),
				"process-payment",
				PaymentRequest{OrderID: event.OrderID},
			)
		},
		retryStrategy,
	)
}

func main() { durable.Start(handler) }
