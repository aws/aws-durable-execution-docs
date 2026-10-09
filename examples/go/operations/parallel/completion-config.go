// Package main completes a Parallel as soon as one branch succeeds.
package main

import "github.com/aws/aws-durable-execution-sdk-go/durable"

func handler(ctx durable.Context, _ any) (*string, error) {
	result, err := durable.Parallel(ctx, "race", []durable.Branch[string]{
		{Func: func(ctx durable.Context) (string, error) {
			return durable.Step(ctx, "source-a", func(durable.StepContext) (string, error) {
				return "result from a", nil
			})
		}},
		{Func: func(ctx durable.Context) (string, error) {
			return durable.Step(ctx, "source-b", func(durable.StepContext) (string, error) {
				return "result from b", nil
			})
		}},
		{Func: func(ctx durable.Context) (string, error) {
			return durable.Step(ctx, "source-c", func(durable.StepContext) (string, error) {
				return "result from c", nil
			})
		}},
	}, durable.WithCompletion(durable.CompletionConfig{MinSuccessful: 1}))
	if err != nil {
		return nil, err
	}
	results := result.Results()
	if len(results) == 0 {
		return nil, nil
	}
	return &results[0], nil
}

func main() { durable.Start(handler) }
