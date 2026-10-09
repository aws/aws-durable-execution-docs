// Package main configures a Parallel with bounded concurrency, a
// first-successful completion policy, and flat nesting.
package main

import "github.com/aws/aws-durable-execution-sdk-go/durable"

func handler(ctx durable.Context, _ any) (*string, error) {
	result, err := durable.Parallel(ctx, "fetch-data", []durable.Branch[string]{
		{Func: func(ctx durable.Context) (string, error) {
			return durable.Step(ctx, "primary", func(durable.StepContext) (string, error) {
				return "primary result", nil
			})
		}},
		{Func: func(ctx durable.Context) (string, error) {
			return durable.Step(ctx, "secondary", func(durable.StepContext) (string, error) {
				return "secondary result", nil
			})
		}},
		{Func: func(ctx durable.Context) (string, error) {
			return durable.Step(ctx, "cache", func(durable.StepContext) (string, error) {
				return "cache result", nil
			})
		}},
	},
		durable.WithMaxConcurrency(2),
		durable.WithCompletion(durable.CompletionConfig{MinSuccessful: 1}),
		durable.WithNesting(durable.NestingFlat),
	)
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
