package main

import "github.com/aws/aws-durable-execution-sdk-go/durable"

type Order struct {
	ID    string `json:"id"`
	Total int    `json:"total"`
}

func chargePayment(order Order) (string, error)    { return "charged-" + order.ID, nil }
func sendConfirmation(order Order) (string, error) { return "emailed-" + order.ID, nil }
func updateInventory(order Order) (string, error)  { return "updated-" + order.ID, nil }

func handler(ctx durable.Context, order Order) (string, error) {
	if _, err := durable.Step(ctx, "charge-payment", func(durable.StepContext) (string, error) {
		return chargePayment(order)
	}); err != nil {
		return "", err
	}
	if _, err := durable.Step(ctx, "send-confirmation", func(durable.StepContext) (string, error) {
		return sendConfirmation(order)
	}); err != nil {
		return "", err
	}
	if _, err := durable.Step(ctx, "update-inventory", func(durable.StepContext) (string, error) {
		return updateInventory(order)
	}); err != nil {
		return "", err
	}
	return "done", nil
}

func main() {
	durable.Start(handler)
}
