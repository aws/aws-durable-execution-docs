package main

import "github.com/aws/aws-durable-execution-sdk-go/durable"

type orderEvent struct {
	OrderID string `json:"orderId"`
	UserID  string `json:"userId"`
}

type validatedOrder struct {
	OrderID string `json:"orderId"`
	UserID  string `json:"userId"`
}

type chargedOrder struct {
	OrderID string `json:"orderId"`
	UserID  string `json:"userId"`
	Charged bool   `json:"charged"`
}

func handler(ctx durable.Context, event orderEvent) (chargedOrder, error) {
	orderID, userID := event.OrderID, event.UserID

	// Capture arguments in a closure.
	return durable.RunInChildContext(ctx, "process-order", func(child durable.Context) (chargedOrder, error) {
		validated, err := durable.Step(child, "validate", func(_ durable.StepContext) (validatedOrder, error) {
			return validate(orderID, userID), nil
		})
		if err != nil {
			return chargedOrder{}, err
		}
		return durable.Step(child, "charge", func(_ durable.StepContext) (chargedOrder, error) {
			return charge(validated), nil
		})
	})
}

func validate(orderID, userID string) validatedOrder {
	return validatedOrder{OrderID: orderID, UserID: userID}
}

func charge(v validatedOrder) chargedOrder {
	return chargedOrder{OrderID: v.OrderID, UserID: v.UserID, Charged: true}
}

func main() { durable.Start(handler) }
