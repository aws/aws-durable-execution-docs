package main

import (
	"github.com/aws/aws-durable-execution-sdk-go/durable"
)

type Order struct {
	ID    string `json:"id"`
	Total string `json:"total"`
}

func handler(ctx durable.Context, _ any) (Order, error) {
	// No serdes option: the SDK serializes and deserializes the result automatically.
	return durable.Step(ctx, "fetch-order", func(durable.StepContext) (Order, error) {
		return Order{ID: "order-123", Total: "99.99"}, nil
	})
}

func main() { durable.Start(handler) }
