// Package main names parallel branches. A branch with an empty Name is named
// "parallel-branch-<index>".
package main

import "github.com/aws/aws-durable-execution-sdk-go/durable"

func taskA(ctx durable.Context) (string, error) {
	return durable.Step(ctx, "run-a", func(durable.StepContext) (string, error) {
		return "a done", nil
	})
}

func handler(ctx durable.Context, _ any) ([]string, error) {
	result, err := durable.Parallel(ctx, "process", []durable.Branch[string]{
		{Func: taskA},
		{Name: "task-b", Func: func(ctx durable.Context) (string, error) {
			return durable.Step(ctx, "run-b", func(durable.StepContext) (string, error) {
				return "b done", nil
			})
		}},
	})
	if err != nil {
		return nil, err
	}
	return result.Results(), nil
}

func main() { durable.Start(handler) }
