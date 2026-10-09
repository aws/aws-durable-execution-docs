package main

import "github.com/aws/aws-durable-execution-sdk-go/durable"

type Order struct {
	ID    string `json:"id"`
	Total int    `json:"total"`
}

type ValidationResult struct {
	Valid bool `json:"valid"`
}

// Define the core logic once, free of SDK types.
func validateOrder(order Order) (ValidationResult, error) {
	return ValidationResult{Valid: order.Total > 0}, nil
}

func handler(ctx durable.Context, order Order) (ValidationResult, error) {
	return durable.Step(ctx, "validate-order", func(durable.StepContext) (ValidationResult, error) {
		return validateOrder(order)
	})
}

func main() {
	durable.Start(handler)
}
