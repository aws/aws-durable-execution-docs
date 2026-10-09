// Save this file as sequential_workflow_test.go and run go test.
package main

import (
	"slices"
	"testing"

	"github.com/aws/aws-durable-execution-sdk-go/durable"
	"github.com/aws/aws-durable-execution-sdk-go/durable/durabletest"
)

type orderEvent struct {
	OrderID string `json:"orderId"`
}

type order struct {
	OrderID     string `json:"orderId"`
	Status      string `json:"status,omitempty"`
	Payment     string `json:"payment,omitempty"`
	Fulfillment string `json:"fulfillment,omitempty"`
}

func sequentialHandler(ctx durable.Context, event orderEvent) (order, error) {
	validated, err := durable.Step[order](ctx, "validate", func(durable.StepContext) (order, error) {
		return order{OrderID: event.OrderID, Status: "validated"}, nil
	})
	if err != nil {
		return order{}, err
	}
	paid, err := durable.Step[order](ctx, "payment", func(durable.StepContext) (order, error) {
		o := validated
		o.Payment = "completed"
		return o, nil
	})
	if err != nil {
		return order{}, err
	}
	return durable.Step[order](ctx, "fulfillment", func(durable.StepContext) (order, error) {
		o := paid
		o.Fulfillment = "shipped"
		return o, nil
	})
}

func TestSequentialWorkflow(t *testing.T) {
	runner := durabletest.NewLocalRunner(sequentialHandler)

	result, err := runner.RunUntilComplete(orderEvent{OrderID: "order-123"})
	if err != nil {
		t.Fatal(err)
	}
	if result.Status != durabletest.Succeeded {
		t.Fatalf("status = %s, want SUCCEEDED\n%s", result.Status, result.FormatTree())
	}

	var names []string
	for _, op := range result.OperationsByType("STEP") {
		names = append(names, op.Name)
	}
	want := []string{"validate", "payment", "fulfillment"}
	if !slices.Equal(names, want) {
		t.Errorf("step names = %v, want %v", names, want)
	}
}
