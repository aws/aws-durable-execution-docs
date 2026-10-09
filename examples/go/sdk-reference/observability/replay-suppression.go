package main

import "github.com/aws/aws-durable-execution-sdk-go/durable"

func handler(ctx durable.Context, event map[string]any) (string, error) {
	ctx.Logger().Info("Step 1 starting")

	result, err := durable.Step(ctx, "step-1", func(sc durable.StepContext) (string, error) {
		return "result", nil
	})
	if err != nil {
		return "", err
	}

	ctx.Logger().Info("Step 1 complete", "result", result)
	return result, nil
}

func main() {
	durable.Start(handler)
}
