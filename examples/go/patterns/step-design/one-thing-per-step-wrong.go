package main

import "github.com/aws/aws-durable-execution-sdk-go/durable"

type Order struct {
	ID    string `json:"id"`
	Total int    `json:"total"`
}

func chargePayment(order Order) (string, error)    { return "charged-" + order.ID, nil }
func sendConfirmation(order Order) (string, error) { return "emailed-" + order.ID, nil }
func updateInventory(order Order) (string, error)  { return "updated-" + order.ID, nil }

// Wrong: one step for three unrelated side effects. A retry repeats all three.
func handler(ctx durable.Context, order Order) (string, error) {
	if _, err := durable.Step(ctx, "process-order", func(durable.StepContext) (struct{}, error) {
		if _, err := chargePayment(order); err != nil {
			return struct{}{}, err
		}
		if _, err := sendConfirmation(order); err != nil {
			return struct{}{}, err
		}
		_, err := updateInventory(order)
		return struct{}{}, err
	}); err != nil {
		return "", err
	}
	return "done", nil
}

func main() {
	durable.Start(handler)
}
