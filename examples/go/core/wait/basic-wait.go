package main

import (
	"time"

	"github.com/aws/aws-durable-execution-sdk-go/durable"
)

func handler(ctx durable.Context, event map[string]any) (string, error) {
	// Wait for 5 seconds. The duration is a standard library time.Duration.
	if err := durable.Wait(ctx, "", 5*time.Second); err != nil {
		return "", err
	}
	return "Wait completed", nil
}

func main() {
	durable.Start(handler)
}
