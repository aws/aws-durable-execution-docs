package main

import (
	"encoding/json"
	"time"

	"github.com/aws/aws-durable-execution-sdk-go/durable"
)

type paymentRequest struct {
	Amount int `json:"amount"`
}

func handler(ctx durable.Context, event paymentRequest) (json.RawMessage, error) {
	cb, err := durable.CreateCallback[json.RawMessage](ctx, "wait-for-payment",
		durable.WithCallbackTimeout(24*time.Hour),
		durable.WithCallbackHeartbeatTimeout(30*time.Minute),
	)
	if err != nil {
		return nil, err
	}

	if err := submitPaymentRequest(cb.ID(), event.Amount); err != nil {
		return nil, err
	}
	return cb.Result(ctx)
}

func submitPaymentRequest(callbackID string, amount int) error {
	return nil
}

func main() { durable.Start(handler) }
