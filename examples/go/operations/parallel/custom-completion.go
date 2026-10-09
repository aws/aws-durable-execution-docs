// Package main stops a Parallel early with a custom completion decision once
// one branch succeeds.
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
	}, durable.WithCompletion(durable.CompletionConfig{
		ShouldComplete: func(p durable.BatchProgress) durable.CompletionDecision {
			if p.SuccessCount >= 1 {
				return durable.CompleteBatch(durable.CompletionOutcomeSucceeded)
			}
			return durable.ContinueBatch()
		},
	}))
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
