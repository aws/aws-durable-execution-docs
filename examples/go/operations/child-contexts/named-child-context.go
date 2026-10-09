package main

import "github.com/aws/aws-durable-execution-sdk-go/durable"

func handler(ctx durable.Context, _ any) (map[string]string, error) {
	unnamed, err := durable.RunInChildContext(ctx, "", func(child durable.Context) (string, error) {
		return durable.Step(child, "", func(_ durable.StepContext) (string, error) {
			return "result", nil
		})
	})
	if err != nil {
		return nil, err
	}

	named, err := durable.RunInChildContext(ctx, "process-order", func(child durable.Context) (string, error) {
		return durable.Step(child, "", func(_ durable.StepContext) (string, error) {
			return "result", nil
		})
	})
	if err != nil {
		return nil, err
	}

	return map[string]string{"unnamed": unnamed, "named": named}, nil
}

func main() { durable.Start(handler) }
