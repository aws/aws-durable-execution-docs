package main

import "github.com/aws/aws-durable-execution-sdk-go/durable"

type Order struct {
	ID    string `json:"id"`
	Total int    `json:"total"`
}

type Receipt struct {
	ID     string `json:"id"`
	Amount int    `json:"amount"`
}

type User struct {
	ID   string `json:"id"`
	Name string `json:"name"`
}

func chargePayment(order Order) (Receipt, error) {
	return Receipt{ID: "rcpt-" + order.ID, Amount: order.Total}, nil
}
func refundPayment(order Order) (Receipt, error) {
	return Receipt{ID: "rfnd-" + order.ID, Amount: -order.Total}, nil
}
func fetchUser(id string) (User, error) { return User{ID: id, Name: "user-" + id}, nil }

func handler(ctx durable.Context, order Order) (string, error) {
	// A side-effecting call runs at most once and never retries.
	paymentOpts := []durable.StepOption{
		durable.WithSemantics(durable.AtMostOncePerRetry),
		durable.WithRetry(durable.NoRetry()),
	}
	// An idempotent read retries with the default exponential backoff.
	idempotentOpts := []durable.StepOption{
		durable.WithRetry(durable.ExponentialBackoff()),
	}

	if _, err := durable.Step(ctx, "charge", func(durable.StepContext) (Receipt, error) {
		return chargePayment(order)
	}, paymentOpts...); err != nil {
		return "", err
	}
	if _, err := durable.Step(ctx, "refund", func(durable.StepContext) (Receipt, error) {
		return refundPayment(order)
	}, paymentOpts...); err != nil {
		return "", err
	}
	user, err := durable.Step(ctx, "fetch-user", func(durable.StepContext) (User, error) {
		return fetchUser(order.ID)
	}, idempotentOpts...)
	if err != nil {
		return "", err
	}
	return user.Name, nil
}

func main() {
	durable.Start(handler)
}
