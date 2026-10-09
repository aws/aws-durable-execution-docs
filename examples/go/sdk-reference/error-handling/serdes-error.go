package main

import (
	"context"

	"github.com/aws/aws-durable-execution-sdk-go/durable"
)

type stepResult struct {
	Message string `json:"message"`
}

// customSerdes delegates to JSONSerdes. A serializer returns an error for a
// value it cannot handle. The SDK fails the operation with a
// *durable.SerdesError and does not retry it. Wrap the error with
// durable.RetryableSerdesError to mark the failure transient, so the backend
// invokes the execution again instead.
type customSerdes struct{}

func (customSerdes) Marshal(ctx context.Context, meta durable.SerdesContext, v any) ([]byte, error) {
	return durable.JSONSerdes.Marshal(ctx, meta, v)
}

func (customSerdes) Unmarshal(ctx context.Context, meta durable.SerdesContext, data []byte, v any) error {
	return durable.JSONSerdes.Unmarshal(ctx, meta, data, v)
}

func handler(ctx durable.Context, _ any) (stepResult, error) {
	return durable.Step(ctx, "build-result", func(_ durable.StepContext) (stepResult, error) {
		return stepResult{Message: "hello"}, nil
	}, durable.WithStepSerdes(customSerdes{}))
}

func main() { durable.Start(handler) }
