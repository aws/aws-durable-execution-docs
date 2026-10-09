package main

import (
	"fmt"
	"time"

	"github.com/aws/aws-durable-execution-sdk-go/durable"
)

func handler(ctx durable.Context, _ any) (string, error) {
	name, err := durable.Step(ctx, "fetch-name", func(_ durable.StepContext) (string, error) {
		return "world", nil
	})
	if err != nil {
		return "", err
	}
	if err := durable.Wait(ctx, "cooldown", 2*time.Second); err != nil {
		return "", err
	}
	return durable.Step(ctx, "format", func(_ durable.StepContext) (string, error) {
		return fmt.Sprintf("hello, %s", name), nil
	})
}

func main() {
	durable.Start(handler)
}
