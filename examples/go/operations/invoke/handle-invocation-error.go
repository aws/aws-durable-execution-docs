package main

import (
	"errors"

	"github.com/aws/aws-durable-execution-sdk-go/durable"
)

type OrderEvent struct {
	OrderID string `json:"orderId"`
}

type Response struct {
	Status string         `json:"status"`
	Reason string         `json:"reason,omitempty"`
	Result map[string]any `json:"result,omitempty"`
}

func handler(ctx durable.Context, event OrderEvent) (Response, error) {
	result, err := durable.Invoke[map[string]any](ctx, "process-payment", "payment-processor-function:live", event)
	if err != nil {
		var invokeErr *durable.InvokeError
		if errors.As(err, &invokeErr) {
			return Response{Status: "failed", Reason: invokeErr.Message}, nil
		}
		return Response{}, err
	}
	return Response{Status: "success", Result: result}, nil
}

func main() {
	durable.Start(handler)
}
