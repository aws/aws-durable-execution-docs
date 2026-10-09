package main

import (
	"errors"

	"github.com/aws/aws-durable-execution-sdk-go/durable"
)

type paymentEvent struct {
	Amount int `json:"amount"`
}

type receipt struct {
	Charged int `json:"charged"`
}

type paymentResult struct {
	Status string   `json:"status"`
	Result *receipt `json:"result,omitempty"`
}

func handler(ctx durable.Context, event paymentEvent) (paymentResult, error) {
	r, err := durable.Step(ctx, "charge-payment", func(_ durable.StepContext) (receipt, error) {
		return chargePayment(event.Amount), nil
	}, durable.WithSemantics(durable.AtMostOncePerRetry))
	if err != nil {
		var interrupted *durable.StepInterruptedError
		if errors.As(err, &interrupted) {
			// Lambda interrupted the step before the SDK recorded an outcome,
			// and no retry remains. Check the payment system to learn whether
			// the charge happened.
			ctx.Logger().Warn("Payment step interrupted; check payment system")
			return paymentResult{Status: "unknown"}, nil
		}
		return paymentResult{}, err
	}
	return paymentResult{Status: "charged", Result: &r}, nil
}

func chargePayment(amount int) receipt {
	return receipt{Charged: amount}
}

func main() { durable.Start(handler) }
