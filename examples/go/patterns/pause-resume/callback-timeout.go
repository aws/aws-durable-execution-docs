package main

import (
	"encoding/json"
	"time"

	"github.com/aws/aws-durable-execution-sdk-go/durable"
)

type Event struct {
	OrderID string `json:"orderId"`
}

func requestApproval(orderID, callbackID string) error { return nil }

func handler(ctx durable.Context, event Event) (json.RawMessage, error) {
	return durable.WaitForCallback[json.RawMessage](ctx, "wait-for-approval",
		func(_ durable.StepContext, callbackID string) error {
			return requestApproval(event.OrderID, callbackID)
		},
		durable.WithCallbackTimeout(24*time.Hour),
	)
}

func main() {
	durable.Start(handler)
}
