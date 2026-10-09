package main

import "github.com/aws/aws-durable-execution-sdk-go/durable"

func handler(ctx durable.Context, event map[string]any) (string, error) {
	// Pass "" as the name for an unnamed step.
	return durable.Step(ctx, "", func(sc durable.StepContext) (string, error) {
		return "some value", nil
	})
}

func main() {
	durable.Start(handler)
}
