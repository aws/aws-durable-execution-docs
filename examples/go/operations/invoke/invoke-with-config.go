package main

import "github.com/aws/aws-durable-execution-sdk-go/durable"

type OrderEvent struct {
	OrderID string `json:"orderId"`
}

type OrderResult struct {
	Status string `json:"status"`
}

func handler(ctx durable.Context, event OrderEvent) (OrderResult, error) {
	return durable.Invoke[OrderResult](
		ctx,
		"process-order",
		"order-processor-function:live",
		event,
		durable.WithTenantID(event.OrderID),
	)
}

func main() {
	durable.Start(handler)
}
