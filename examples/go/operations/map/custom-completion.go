// Package main stops a Map early with a custom completion decision once two
// items succeed.
package main

import (
	"fmt"
	"strings"

	"github.com/aws/aws-durable-execution-sdk-go/durable"
)

type Input struct {
	Items []string `json:"items"`
}

func handler(ctx durable.Context, event Input) ([]string, error) {
	result, err := durable.Map(ctx, "process-items", event.Items,
		func(ctx durable.Context, item string, index int) (string, error) {
			return durable.Step(ctx, fmt.Sprintf("process-%d", index),
				func(durable.StepContext) (string, error) {
					return strings.ToUpper(item), nil
				})
		},
		durable.WithCompletion(durable.CompletionConfig{
			ShouldComplete: func(p durable.BatchProgress) durable.CompletionDecision {
				if p.SuccessCount >= 2 {
					return durable.CompleteBatch(durable.CompletionOutcomeSucceeded)
				}
				return durable.ContinueBatch()
			},
		}),
	)
	if err != nil {
		return nil, err
	}
	return result.Results(), nil
}

func main() { durable.Start(handler) }
