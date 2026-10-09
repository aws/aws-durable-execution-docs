// Package main shows the item function a Map applies to each item. The
// function runs steps in the item's own child context and returns a value.
package main

import (
	"errors"

	"github.com/aws/aws-durable-execution-sdk-go/durable"
)

type Order struct {
	ID     string `json:"id"`
	Amount int    `json:"amount"`
}

type Receipt struct {
	OrderID string `json:"orderId"`
	Charged int    `json:"charged"`
}

type Input struct {
	Orders []Order `json:"orders"`
}

func processOrder(ctx durable.Context, order Order, index int) (Receipt, error) {
	validated, err := durable.Step(ctx, "validate", func(durable.StepContext) (Order, error) {
		if order.Amount <= 0 {
			return Order{}, errors.New("invalid amount")
		}
		return order, nil
	})
	if err != nil {
		return Receipt{}, err
	}
	charged, err := durable.Step(ctx, "charge", func(durable.StepContext) (int, error) {
		return validated.Amount, nil
	})
	if err != nil {
		return Receipt{}, err
	}
	return Receipt{OrderID: validated.ID, Charged: charged}, nil
}

func handler(ctx durable.Context, event Input) ([]Receipt, error) {
	result, err := durable.Map(ctx, "process-orders", event.Orders, processOrder)
	if err != nil {
		return nil, err
	}
	return result.Results(), nil
}

func main() { durable.Start(handler) }
