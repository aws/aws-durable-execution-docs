package main

import (
	"crypto/rand"

	"github.com/aws/aws-durable-execution-sdk-go/durable"
)

type Event struct {
	Amount int `json:"amount"`
}

type Receipt struct {
	TransactionID string `json:"transactionId"`
	Amount        int    `json:"amount"`
}

type Result struct {
	TransactionID string  `json:"transactionId"`
	Receipt       Receipt `json:"receipt"`
}

func charge(amount int, transactionID string) (Receipt, error) {
	return Receipt{TransactionID: transactionID, Amount: amount}, nil
}

func handler(ctx durable.Context, event Event) (Result, error) {
	transactionID, err := durable.Step(ctx, "generate-transaction-id", func(durable.StepContext) (string, error) {
		return rand.Text(), nil
	})
	if err != nil {
		return Result{}, err
	}

	receipt, err := durable.Step(ctx, "charge", func(durable.StepContext) (Receipt, error) {
		return charge(event.Amount, transactionID)
	})
	if err != nil {
		return Result{}, err
	}

	return Result{TransactionID: transactionID, Receipt: receipt}, nil
}

func main() {
	durable.Start(handler)
}
