package main

import "github.com/aws/aws-durable-execution-sdk-go/durable"

func handler(ctx durable.Context, _ any) (string, error) {
	fut := durable.Go(ctx, "work", func(child durable.Context) (string, error) {
		return durable.Step(child, "step", func(_ durable.StepContext) (string, error) {
			return "done", nil
		})
	})
	return fut.Result(ctx)
}

func main() {
	durable.Start(handler)
}
