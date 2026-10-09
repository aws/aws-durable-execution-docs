package main

import "github.com/aws/aws-durable-execution-sdk-go/durable"

type Order struct {
	ID        string `json:"id"`
	Total     int    `json:"total"`
	CardToken string `json:"cardToken"`
	Address   string `json:"address"`
}

type ValidationResult struct {
	Valid bool `json:"valid"`
}

type Receipt struct {
	ID     string `json:"id"`
	Amount int    `json:"amount"`
}

type Result struct {
	Receipt    Receipt `json:"receipt"`
	ShipmentID string  `json:"shipmentId"`
}

// Business logic takes plain arguments and returns plain values. No durable context.
func validateOrder(order Order) (ValidationResult, error) {
	return ValidationResult{Valid: order.Total > 0}, nil
}

func chargePayment(order Order) (Receipt, error) {
	return Receipt{ID: "rcpt-" + order.ID, Amount: order.Total}, nil
}

func scheduleShipment(order Order) (string, error) {
	return "ship-" + order.ID, nil
}

// The handler orchestrates: it decides when each step runs.
func handler(ctx durable.Context, order Order) (Result, error) {
	if _, err := durable.Step(ctx, "validate", func(durable.StepContext) (ValidationResult, error) {
		return validateOrder(order)
	}); err != nil {
		return Result{}, err
	}
	receipt, err := durable.Step(ctx, "charge", func(durable.StepContext) (Receipt, error) {
		return chargePayment(order)
	})
	if err != nil {
		return Result{}, err
	}
	shipmentID, err := durable.Step(ctx, "schedule", func(durable.StepContext) (string, error) {
		return scheduleShipment(order)
	})
	if err != nil {
		return Result{}, err
	}
	return Result{Receipt: receipt, ShipmentID: shipmentID}, nil
}

func main() {
	durable.Start(handler)
}
