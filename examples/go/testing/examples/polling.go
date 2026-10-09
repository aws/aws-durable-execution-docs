// Save this file as polling_test.go and run go test.
package main

import (
	"testing"
	"time"

	"github.com/aws/aws-durable-execution-sdk-go/durable"
	"github.com/aws/aws-durable-execution-sdk-go/durable/durabletest"
)

type pollState struct {
	Attempts int  `json:"attempts"`
	Done     bool `json:"done"`
}

func pollingHandler(ctx durable.Context, _ any) (pollState, error) {
	return durable.WaitForCondition[pollState](ctx, "poll-job",
		func(_ durable.StepContext, state pollState) (pollState, error) {
			return pollState{Attempts: state.Attempts + 1, Done: state.Attempts >= 2}, nil
		},
		durable.ConditionConfig[pollState]{
			InitialState: pollState{Attempts: 0, Done: false},
			WaitStrategy: func(state pollState, _ int) durable.WaitDecision {
				if state.Done {
					return durable.WaitDecision{}
				}
				return durable.WaitDecision{Continue: true, Delay: time.Second}
			},
		},
	)
}

func TestPolling(t *testing.T) {
	runner := durabletest.NewLocalRunner(pollingHandler)

	result, err := runner.RunUntilComplete(nil)
	if err != nil {
		t.Fatal(err)
	}
	if result.Status != durabletest.Succeeded {
		t.Fatalf("status = %s, want SUCCEEDED\n%s", result.Status, result.FormatTree())
	}

	out, err := durabletest.ResultAs[pollState](result)
	if err != nil {
		t.Fatal(err)
	}
	if !out.Done {
		t.Error("done = false, want true")
	}
}
