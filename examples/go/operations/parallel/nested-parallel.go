// Package main nests a Parallel inside a Parallel branch. Each nested
// Parallel creates its own set of child contexts.
package main

import "github.com/aws/aws-durable-execution-sdk-go/durable"

func handler(ctx durable.Context, _ any) ([][]string, error) {
	result, err := durable.Parallel(ctx, "outer", []durable.Branch[[]string]{
		{Func: func(ctx durable.Context) ([]string, error) {
			inner, err := durable.Parallel(ctx, "inner-a", []durable.Branch[string]{
				{Func: func(ctx durable.Context) (string, error) {
					return durable.Step(ctx, "a1", func(durable.StepContext) (string, error) { return "a1", nil })
				}},
				{Func: func(ctx durable.Context) (string, error) {
					return durable.Step(ctx, "a2", func(durable.StepContext) (string, error) { return "a2", nil })
				}},
			})
			if err != nil {
				return nil, err
			}
			return inner.Results(), nil
		}},
		{Func: func(ctx durable.Context) ([]string, error) {
			inner, err := durable.Parallel(ctx, "inner-b", []durable.Branch[string]{
				{Func: func(ctx durable.Context) (string, error) {
					return durable.Step(ctx, "b1", func(durable.StepContext) (string, error) { return "b1", nil })
				}},
				{Func: func(ctx durable.Context) (string, error) {
					return durable.Step(ctx, "b2", func(durable.StepContext) (string, error) { return "b2", nil })
				}},
			})
			if err != nil {
				return nil, err
			}
			return inner.Results(), nil
		}},
	})
	if err != nil {
		return nil, err
	}
	return result.Results(), nil
}

func main() { durable.Start(handler) }
