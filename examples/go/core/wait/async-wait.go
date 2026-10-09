package main

import (
	"time"

	"github.com/aws/aws-durable-execution-sdk-go/durable"
)

func process(event map[string]any) (string, error) {
	return "processed", nil
}

func handler(ctx durable.Context, event map[string]any) (string, error) {
	// Start the wait and the step without awaiting either yet.
	waitFut := durable.WaitAsync(ctx, "min-delay", 5*time.Second)
	stepFut := durable.StepAsync(ctx, "process", func(sc durable.StepContext) (string, error) {
		return process(event)
	})

	// Await both with a combinator so a suspension drains every branch.
	// This guarantees at least 5 seconds elapsed.
	if err := durable.Join(ctx, "min-delay-and-process", []durable.Awaitable{waitFut, stepFut}); err != nil {
		return "", err
	}
	return stepFut.Result(ctx)
}

func main() {
	durable.Start(handler)
}
