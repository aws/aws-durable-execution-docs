package main

import (
	"time"

	"github.com/aws/aws-durable-execution-sdk-go/durable"
)

func handler(ctx durable.Context, _ any) (string, error) {
	if err := durable.Wait(ctx, "cool-off", 24*time.Hour); err != nil {
		return "", err
	}
	return "resumed", nil
}

func main() {
	durable.Start(handler)
}
