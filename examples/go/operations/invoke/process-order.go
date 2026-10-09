package main

import "github.com/aws/aws-durable-execution-sdk-go/durable"

type OrderEvent struct {
	OrderID string `json:"orderId"`
	Amount  int    `json:"amount"`
}

type ValidationResult struct {
	Valid  bool   `json:"valid"`
	Reason string `json:"reason,omitempty"`
}

type PaymentResult struct {
	TransactionID string `json:"transactionId"`
}

type OrderResult struct {
	Status        string `json:"status"`
	Reason        string `json:"reason,omitempty"`
	TransactionID string `json:"transactionId,omitempty"`
}

func handler(ctx durable.Context, event OrderEvent) (OrderResult, error) {
	validation, err := durable.Invoke[ValidationResult](ctx, "validate-order", "validate-order-function:live", event)
	if err != nil {
		return OrderResult{}, err
	}
	if !validation.Valid {
		return OrderResult{Status: "rejected", Reason: validation.Reason}, nil
	}

	payment, err := durable.Invoke[PaymentResult](ctx, "process-payment", "payment-processor-function:live", event)
	if err != nil {
		return OrderResult{}, err
	}
	return OrderResult{Status: "completed", TransactionID: payment.TransactionID}, nil
}

func main() {
	durable.Start(handler)
}
