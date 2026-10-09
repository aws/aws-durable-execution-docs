package main

import "github.com/aws/aws-durable-execution-sdk-go/durable"

type User struct {
	ID   string `json:"id"`
	Name string `json:"name"`
}

type Event struct {
	User      User   `json:"user"`
	Amount    int    `json:"amount"`
	CardToken string `json:"cardToken"`
}

type Receipt struct {
	ID     string `json:"id"`
	Amount int    `json:"amount"`
}

func upsertUser(u User) (string, error) { return "upserted-" + u.ID, nil }

func chargePayment(amount int, cardToken string) (Receipt, error) {
	return Receipt{ID: "rcpt-" + cardToken, Amount: amount}, nil
}

func handler(ctx durable.Context, event Event) (string, error) {
	// At-least-once (default): the upsert is idempotent, so a replay can re-run it.
	if _, err := durable.Step(ctx, "upsert-user", func(durable.StepContext) (string, error) {
		return upsertUser(event.User)
	}); err != nil {
		return "", err
	}

	// At-most-once with retries disabled: the charge runs once end to end.
	if _, err := durable.Step(ctx, "charge-payment", func(durable.StepContext) (Receipt, error) {
		return chargePayment(event.Amount, event.CardToken)
	}, durable.WithSemantics(durable.AtMostOncePerRetry), durable.WithRetry(durable.NoRetry())); err != nil {
		return "", err
	}

	return "done", nil
}

func main() {
	durable.Start(handler)
}
