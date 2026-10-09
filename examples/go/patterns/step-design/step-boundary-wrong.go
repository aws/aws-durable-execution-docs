package main

import "github.com/aws/aws-durable-execution-sdk-go/durable"

func work() (string, error) { return "worked", nil }

// Wrong: the step body starts an operation on the captured outer context.
// The inner call fails with durable.ErrWrongContext.
func handler(ctx durable.Context, _ any) (string, error) {
	return durable.Step(ctx, "outer", func(durable.StepContext) (string, error) {
		return durable.Step(ctx, "inner", func(durable.StepContext) (string, error) {
			return work()
		})
	})
}

func main() {
	durable.Start(handler)
}
