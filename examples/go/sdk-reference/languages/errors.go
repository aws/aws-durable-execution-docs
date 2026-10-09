package main

import (
	"errors"

	"github.com/aws/aws-durable-execution-sdk-go/durable"
)

type CardDeclinedError struct{}

func (CardDeclinedError) Error() string { return "card declined" }

func handler(ctx durable.Context, _ any) (string, error) {
	receipt, err := durable.Step(ctx, "charge", func(_ durable.StepContext) (string, error) {
		return "", CardDeclinedError{}
	}, durable.WithRetry(durable.NoRetry()))

	var stepErr *durable.StepError
	switch {
	case err == nil:
		return receipt, nil
	case errors.As(err, &stepErr) && stepErr.ErrorType == "CardDeclinedError":
		return "declined", nil
	default:
		return "", err
	}
}

func main() {
	durable.Start(handler)
}
