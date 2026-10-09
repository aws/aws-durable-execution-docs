package main

import (
	"time"

	"github.com/aws/aws-durable-execution-sdk-go/durable"
)

func handler(ctx durable.Context, _ any) (map[string]string, error) {
	// The SDK returns a plain error for invalid configuration values.
	// For example, passing a negative delay to NewRetryStrategy:
	_, err := durable.NewRetryStrategy(durable.RetryConfig{
		InitialDelay: -1 * time.Second, // invalid: negative delay
		MaxAttempts:  3,
	})
	if err != nil {
		ctx.Logger().Error("Invalid SDK configuration", "message", err.Error())
		return map[string]string{"error": "InvalidConfiguration"}, nil
	}
	return map[string]string{"status": "ok"}, nil
}

func main() { durable.Start(handler) }
