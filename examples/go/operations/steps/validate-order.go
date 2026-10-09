package main

import "github.com/aws/aws-durable-execution-sdk-go/durable"

type OrderEvent struct {
	OrderID string `json:"order_id"`
}

type Validation struct {
	OrderID string `json:"order_id"`
	Valid   bool   `json:"valid"`
}

func validateOrder(ctx durable.StepContext, orderID string) (Validation, error) {
	return Validation{OrderID: orderID, Valid: true}, nil
}

func handler(ctx durable.Context, event OrderEvent) (Validation, error) {
	return durable.Step(ctx, "validate_order", func(sc durable.StepContext) (Validation, error) {
		return validateOrder(sc, event.OrderID)
	})
}

func main() {
	durable.Start(handler)
}
