package main

import (
	"errors"
	"fmt"

	"github.com/aws/aws-durable-execution-sdk-go/durable"
)

type report struct {
	MatchedStepError      bool   `json:"matchedStepError"`
	MatchedOperationError bool   `json:"matchedOperationError"`
	Name                  string `json:"name"`
	ErrorType             string `json:"errorType"`
}

func handler(ctx durable.Context, _ any) (report, error) {
	_, err := durable.Step(ctx, "failing-step", func(_ durable.StepContext) (any, error) {
		return nil, fmt.Errorf("deliberate failure")
	}, durable.WithRetry(durable.NoRetry()))

	var r report
	var stepErr *durable.StepError
	if errors.As(err, &stepErr) {
		r.MatchedStepError = true
		r.Name = stepErr.Name
		r.ErrorType = stepErr.ErrorType
	}
	var opErr *durable.OperationError
	if errors.As(err, &opErr) {
		r.MatchedOperationError = true
	}
	return r, nil
}

func main() { durable.Start(handler) }
