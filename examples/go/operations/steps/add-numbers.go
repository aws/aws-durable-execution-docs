package main

import "github.com/aws/aws-durable-execution-sdk-go/durable"

func addNumbers(ctx durable.StepContext, a, b int) (int, error) {
	return a + b, nil
}

func handler(ctx durable.Context, event map[string]any) (int, error) {
	return durable.Step(ctx, "add_numbers", func(sc durable.StepContext) (int, error) {
		return addNumbers(sc, 5, 3)
	})
}

func main() {
	durable.Start(handler)
}
