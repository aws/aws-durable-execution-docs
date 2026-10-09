package main

import (
	"time"

	"github.com/aws/aws-durable-execution-sdk-go/durable"
)

func handler(ctx durable.Context, event map[string]any) (string, error) {
	// The name is the second argument, after the context.
	if err := durable.Wait(ctx, "custom_wait", 2*time.Second); err != nil {
		return "", err
	}
	return "Wait with name completed", nil
}

func main() {
	durable.Start(handler)
}
