package main

import "github.com/aws/aws-durable-execution-sdk-go/durable"

func handler(ctx durable.Context, event map[string]any) (string, error) {
	ctx.Logger().Info("Starting workflow")

	result, err := durable.Step(ctx, "process", func(sc durable.StepContext) (string, error) {
		return "done", nil
	})
	if err != nil {
		return "", err
	}

	ctx.Logger().Info("Workflow complete", "result", result)
	return result, nil
}

func main() {
	durable.Start(handler)
}
