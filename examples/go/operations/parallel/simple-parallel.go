// Package main runs independent branches concurrently with Parallel and
// returns their results in input order.
package main

import "github.com/aws/aws-durable-execution-sdk-go/durable"

func handler(ctx durable.Context, _ any) ([]string, error) {
	result, err := durable.Parallel(ctx, "check-services", []durable.Branch[string]{
		{Func: func(ctx durable.Context) (string, error) {
			return durable.Step(ctx, "check-inventory", func(durable.StepContext) (string, error) {
				return "inventory ok", nil
			})
		}},
		{Func: func(ctx durable.Context) (string, error) {
			return durable.Step(ctx, "check-payment", func(durable.StepContext) (string, error) {
				return "payment ok", nil
			})
		}},
		{Func: func(ctx durable.Context) (string, error) {
			return durable.Step(ctx, "check-shipping", func(durable.StepContext) (string, error) {
				return "shipping ok", nil
			})
		}},
	})
	if err != nil {
		return nil, err
	}
	return result.Results(), nil
}

func main() { durable.Start(handler) }
