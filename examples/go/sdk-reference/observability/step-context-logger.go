package main

import "github.com/aws/aws-durable-execution-sdk-go/durable"

func handler(ctx durable.Context, event map[string]any) (string, error) {
	return durable.Step(ctx, "process", func(sc durable.StepContext) (string, error) {
		sc.Logger().Info("Running step")
		return "done", nil
	})
}

func main() {
	durable.Start(handler)
}
