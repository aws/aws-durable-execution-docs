package main

import (
	"errors"

	"github.com/aws/aws-durable-execution-sdk-go/durable"
)

type orderEvent struct {
	OrderID string `json:"orderId"`
}

func handler(ctx durable.Context, event orderEvent) (map[string]string, error) {
	result, err := durable.Step(ctx, "process-order", func(_ durable.StepContext) (map[string]string, error) {
		if event.OrderID == "" {
			return nil, errors.New("orderId is required")
		}
		return map[string]string{"orderId": event.OrderID, "status": "processed"}, nil
	})
	if err != nil {
		var stepErr *durable.StepError
		if errors.As(err, &stepErr) {
			ctx.Logger().Error("Step failed", "cause", stepErr.Message)
			return map[string]string{"error": stepErr.Message}, nil
		}
		return nil, err
	}
	return result, nil
}

func main() { durable.Start(handler) }
