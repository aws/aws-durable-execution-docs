package main

import (
	"encoding/json"
	"time"

	"github.com/aws/aws-durable-execution-sdk-go/durable"
)

type Event struct {
	JobID string `json:"jobId"`
}

func startJob(jobID, callbackID string) error { return nil }

func handler(ctx durable.Context, event Event) (json.RawMessage, error) {
	return durable.WaitForCallback[json.RawMessage](ctx, "long-running-job",
		func(_ durable.StepContext, callbackID string) error {
			return startJob(event.JobID, callbackID)
		},
		durable.WithCallbackTimeout(24*time.Hour),
		durable.WithCallbackHeartbeatTimeout(10*time.Minute),
	)
}

func main() {
	durable.Start(handler)
}
