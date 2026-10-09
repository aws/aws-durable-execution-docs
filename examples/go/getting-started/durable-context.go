package main

import "github.com/aws/aws-durable-execution-sdk-go/durable"

// handler receives a durable.Context in place of the standard Lambda
// context. It passes that context to the package-level operation
// functions, such as durable.Step.
func handler(ctx durable.Context, _ any) (string, error) {
	return durable.Step(ctx, "my-step", func(_ durable.StepContext) (string, error) {
		return "step completed", nil
	})
}

func main() {
	durable.Start(handler)
}
