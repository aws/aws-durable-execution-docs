package main

import (
	"time"

	"github.com/aws/aws-durable-execution-sdk-go/durable"
)

type Response struct {
	StatusCode int    `json:"statusCode"`
	Body       string `json:"body"`
}

func handler(ctx durable.Context, _ any) (Response, error) {
	message, err := durable.Step(ctx, "step-1", func(sc durable.StepContext) (string, error) {
		sc.Logger().Info("Hello from step-1")
		return "Hello from Durable Lambda!", nil
	})
	if err != nil {
		return Response{}, err
	}

	// Pause for 10 seconds without consuming compute or incurring usage charges.
	if err := durable.Wait(ctx, "wait-10s", 10*time.Second); err != nil {
		return Response{}, err
	}

	// Replay-aware: this logs once even though the function replays after the wait.
	ctx.Logger().Info("Resumed after wait")

	return Response{StatusCode: 200, Body: message}, nil
}

func main() {
	durable.Start(handler)
}
