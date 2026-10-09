package main

import "github.com/aws/aws-durable-execution-sdk-go/durable"

type order struct {
	OrderID string `json:"orderId"`
}

type validatedOrder struct {
	OrderID string `json:"orderId"`
	Valid   bool   `json:"valid"`
}

type chargedOrder struct {
	OrderID string `json:"orderId"`
	Valid   bool   `json:"valid"`
	Charged bool   `json:"charged"`
}

func handler(ctx durable.Context, event order) (chargedOrder, error) {
	return durable.RunInChildContext(ctx, "process-order", func(child durable.Context) (chargedOrder, error) {
		v, err := durable.Step(child, "validate", func(_ durable.StepContext) (validatedOrder, error) {
			return validate(event.OrderID), nil
		})
		if err != nil {
			return chargedOrder{}, err
		}
		return durable.Step(child, "charge", func(_ durable.StepContext) (chargedOrder, error) {
			return charge(v), nil
		})
	})
}

func validate(orderID string) validatedOrder {
	return validatedOrder{OrderID: orderID, Valid: true}
}

func charge(v validatedOrder) chargedOrder {
	return chargedOrder{OrderID: v.OrderID, Valid: v.Valid, Charged: true}
}

func main() { durable.Start(handler) }
