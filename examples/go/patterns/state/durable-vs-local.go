package main

import "github.com/aws/aws-durable-execution-sdk-go/durable"

type Result struct {
	Value string `json:"value"`
}

func fetchValue() (string, error) { return "fetched", nil }

func handler(ctx durable.Context, _ any) (Result, error) {
	// The step return value is checkpointed as durable state.
	value, err := durable.Step(ctx, "fetch", func(durable.StepContext) (string, error) {
		return fetchValue()
	})
	if err != nil {
		return Result{}, err
	}

	// A local variable that holds the value costs nothing until the handler
	// returns it, which serializes it into execution state.
	return Result{Value: value}, nil
}

func main() {
	durable.Start(handler)
}
