// Package main reads branch failures from a Parallel result. Tolerating a
// failure keeps every branch running so the result lists both outcomes.
package main

import (
	"errors"

	"github.com/aws/aws-durable-execution-sdk-go/durable"
	"github.com/aws/aws-sdk-go-v2/aws"
)

type Output struct {
	Succeeded int      `json:"succeeded"`
	Failed    int      `json:"failed"`
	Results   []string `json:"results"`
	Errors    []string `json:"errors"`
}

func handler(ctx durable.Context, _ any) (Output, error) {
	result, err := durable.Parallel(ctx, "tasks", []durable.Branch[string]{
		{Func: func(ctx durable.Context) (string, error) {
			return durable.Step(ctx, "task-1", func(durable.StepContext) (string, error) {
				return "ok", nil
			})
		}},
		{Func: func(ctx durable.Context) (string, error) {
			return durable.Step(ctx, "task-2", func(durable.StepContext) (string, error) {
				return "", errors.New("task 2 failed")
			})
		}},
		{Func: func(ctx durable.Context) (string, error) {
			return durable.Step(ctx, "task-3", func(durable.StepContext) (string, error) {
				return "ok", nil
			})
		}},
	}, durable.WithCompletion(durable.CompletionConfig{ToleratedFailureCount: aws.Int(1)}))
	if err != nil {
		return Output{}, err
	}

	messages := make([]string, 0, result.FailureCount())
	for _, e := range result.Errors() {
		messages = append(messages, e.Error())
	}
	return Output{
		Succeeded: result.SuccessCount(),
		Failed:    result.FailureCount(),
		Results:   result.Results(),
		Errors:    messages,
	}, nil
}

func main() { durable.Start(handler) }
