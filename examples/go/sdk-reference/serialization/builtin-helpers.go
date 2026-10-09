package main

import (
	"github.com/aws/aws-durable-execution-sdk-go/durable"
)

// RawSerdes stores the value as-is (it must be a string, []byte, or json.RawMessage).
// JSONSerdes is the default: encoding/json without HTML escaping.

func handler(ctx durable.Context, _ any) (string, error) {
	return durable.Step(ctx, "fetch_raw", func(durable.StepContext) (string, error) {
		return `{"id":"order-123"}`, nil
	}, durable.WithStepSerdes(durable.RawSerdes))
}

func main() { durable.Start(handler) }
