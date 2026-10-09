package main

import "github.com/aws/aws-durable-execution-sdk-go/durable"

type resultPair struct {
	A string `json:"a"`
	B string `json:"b"`
}

func handler(ctx durable.Context, _ any) (resultPair, error) {
	a := durable.Go(ctx, "branch-a", func(child durable.Context) (string, error) {
		return durable.Step(child, "fetch-a", func(_ durable.StepContext) (string, error) {
			return "result-a", nil
		})
	})
	b := durable.Go(ctx, "branch-b", func(child durable.Context) (string, error) {
		return durable.Step(child, "fetch-b", func(_ durable.StepContext) (string, error) {
			return "result-b", nil
		})
	})

	ra, err := a.Result(ctx)
	if err != nil {
		return resultPair{}, err
	}
	rb, err := b.Result(ctx)
	if err != nil {
		return resultPair{}, err
	}
	return resultPair{A: ra, B: rb}, nil
}

func main() { durable.Start(handler) }
