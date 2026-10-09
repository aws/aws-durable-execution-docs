package main

import (
	"crypto/rand"

	"github.com/aws/aws-durable-execution-sdk-go/durable"
)

type Event struct {
	Amount    int    `json:"amount"`
	CardToken string `json:"cardToken"`
}

type Charge struct {
	IdempotencyKey string `json:"idempotencyKey"`
	Amount         int    `json:"amount"`
}

func charge(amount int, cardToken, idempotencyKey string) (Charge, error) {
	return Charge{IdempotencyKey: idempotencyKey, Amount: amount}, nil
}

func handler(ctx durable.Context, event Event) (Charge, error) {
	idempotencyKey, err := durable.Step(ctx, "idempotency-key", func(durable.StepContext) (string, error) {
		return rand.Text(), nil
	})
	if err != nil {
		return Charge{}, err
	}

	return durable.Step(ctx, "charge", func(durable.StepContext) (Charge, error) {
		return charge(event.Amount, event.CardToken, idempotencyKey)
	})
}

func main() {
	durable.Start(handler)
}
