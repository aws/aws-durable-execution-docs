package main

import (
	"time"

	"github.com/aws/aws-durable-execution-sdk-go/durable"
)

type EmailEvent struct {
	Email string `json:"email"`
}

func registerUser(email string) (string, error) {
	return "user-" + email, nil
}

func sendFollowUpEmail(userID string) error {
	// send email to user
	return nil
}

// WRONG: the mutation of userID is lost on replay after the wait.
func handler(ctx durable.Context, event EmailEvent) (durable.Void, error) {
	userID := ""

	if _, err := durable.Step(ctx, "register-user", func(sc durable.StepContext) (durable.Void, error) {
		id, err := registerUser(event.Email)
		userID = id // lost on replay
		return durable.Void{}, err
	}); err != nil {
		return durable.Void{}, err
	}

	if err := durable.Wait(ctx, "follow-up-delay", 10*time.Minute); err != nil {
		return durable.Void{}, err
	}

	return durable.Step(ctx, "send-follow-up-email", func(sc durable.StepContext) (durable.Void, error) {
		return durable.Void{}, sendFollowUpEmail(userID) // userID is "" on replay
	})
}

func main() {
	durable.Start(handler)
}
