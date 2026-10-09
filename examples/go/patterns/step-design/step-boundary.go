package main

import "github.com/aws/aws-durable-execution-sdk-go/durable"

type Order struct {
	ID string `json:"id"`
}

func validate(order Order) (bool, error) { return order.ID != "", nil }
func charge(order Order) (string, error) { return "charged-" + order.ID, nil }

func handler(ctx durable.Context, order Order) (string, error) {
	return durable.RunInChildContext(ctx, "order-pipeline", func(child durable.Context) (string, error) {
		if _, err := durable.Step(child, "validate", func(durable.StepContext) (bool, error) {
			return validate(order)
		}); err != nil {
			return "", err
		}
		if _, err := durable.Step(child, "charge", func(durable.StepContext) (string, error) {
			return charge(order)
		}); err != nil {
			return "", err
		}
		return "done", nil
	})
}

func main() {
	durable.Start(handler)
}
