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

// CORRECT: userID is returned from the step and restored from the checkpoint on replay.
func handler(ctx durable.Context, event EmailEvent) (durable.Void, error) {
	userID, err := durable.Step(ctx, "register-user", func(sc durable.StepContext) (string, error) {
		return registerUser(event.Email)
	})
	if err != nil {
		return durable.Void{}, err
	}

	if err := durable.Wait(ctx, "follow-up-delay", 10*time.Minute); err != nil {
		return durable.Void{}, err
	}

	return durable.Step(ctx, "send-follow-up-email", func(sc durable.StepContext) (durable.Void, error) {
		return durable.Void{}, sendFollowUpEmail(userID) // userID restored from the checkpoint
	})
}

func main() {
	durable.Start(handler)
}
