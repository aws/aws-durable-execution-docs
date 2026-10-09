package main

import (
	"time"

	"github.com/aws/aws-durable-execution-sdk-go/durable"
)

type Event struct {
	ID string `json:"id"`
}

func handler(ctx durable.Context, event Event) (string, error) {
	// Step 1: fetch data. The SDK checkpoints the result.
	data, err := durable.Step(ctx, "fetch-data", func(_ durable.StepContext) (string, error) {
		return fetchData(event.ID), nil
	})
	if err != nil {
		return "", err
	}

	// Step 2: wait 30 seconds without consuming compute.
	if err := durable.Wait(ctx, "wait-30s", 30*time.Second); err != nil {
		return "", err
	}

	// Step 3: process the data. It runs only after the wait completes.
	return durable.Step(ctx, "process-data", func(_ durable.StepContext) (string, error) {
		return processData(data), nil
	})
}

func fetchData(id string) string {
	return "data-for-" + id
}

func processData(data string) string {
	return "processed-" + data
}

func main() {
	durable.Start(handler)
}
