package main

import (
	"context"
	"encoding/json"

	"github.com/aws/aws-durable-execution-sdk-go/durable"
)

type Order struct {
	ID    string `json:"id"`
	Total string `json:"total"`
}

var orderSerdes = durable.SerdesOf(
	func(_ context.Context, _ durable.SerdesContext, o Order) ([]byte, error) {
		return json.Marshal(o)
	},
	func(_ context.Context, _ durable.SerdesContext, data []byte) (Order, error) {
		var o Order
		err := json.Unmarshal(data, &o)
		return o, err
	},
)

func handler(ctx durable.Context, _ any) (Order, error) {
	return durable.Step(ctx, "fetch-order", func(durable.StepContext) (Order, error) {
		return Order{ID: "order-123", Total: "99.99"}, nil
	}, durable.WithStepSerdes(orderSerdes))
}

func main() { durable.Start(handler) }
