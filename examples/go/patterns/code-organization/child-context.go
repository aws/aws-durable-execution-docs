package main

import "github.com/aws/aws-durable-execution-sdk-go/durable"

type Order struct {
	ID    string `json:"id"`
	Total int    `json:"total"`
}

type Receipt struct {
	ID     string `json:"id"`
	Amount int    `json:"amount"`
}

func validate(order Order) (bool, error) { return order.Total > 0, nil }
func charge(order Order) (Receipt, error) {
	return Receipt{ID: "rcpt-" + order.ID, Amount: order.Total}, nil
}
func schedule(order Order, r Receipt) (string, error) { return "ship-" + r.ID, nil }

func handler(ctx durable.Context, order Order) (string, error) {
	return durable.RunInChildContext(ctx, "process-order", func(child durable.Context) (string, error) {
		if _, err := durable.Step(child, "validate", func(durable.StepContext) (bool, error) {
			return validate(order)
		}); err != nil {
			return "", err
		}
		receipt, err := durable.Step(child, "charge", func(durable.StepContext) (Receipt, error) {
			return charge(order)
		})
		if err != nil {
			return "", err
		}
		if _, err := durable.Step(child, "schedule", func(durable.StepContext) (string, error) {
			return schedule(order, receipt)
		}); err != nil {
			return "", err
		}
		return "ok", nil
	})
}

func main() {
	durable.Start(handler)
}
