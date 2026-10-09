// Save this file as assert_callback_test.go and run go test.
package main

import (
	"encoding/json"
	"testing"

	"github.com/aws/aws-durable-execution-sdk-go/durable"
	"github.com/aws/aws-durable-execution-sdk-go/durable/durabletest"
)

type approval struct {
	Approved bool `json:"approved"`
}

func callbackHandler(ctx durable.Context, _ any) (string, error) {
	// The default callback deserializer is RawSerdes, so WaitForCallback
	// returns the submitted bytes unchanged as a string. The test parses them.
	return durable.WaitForCallback[string](ctx, "approval",
		func(durable.StepContext, string) error {
			// In production the submitter hands the callback ID to an
			// external system. The test completes the callback directly.
			return nil
		},
	)
}

func TestAssertCallback(t *testing.T) {
	runner := durabletest.NewLocalRunner(callbackHandler)

	result, err := runner.RunUntilComplete(nil)
	if err != nil {
		t.Fatal(err)
	}
	if result.Status != durabletest.Pending {
		t.Fatalf("status = %s, want PENDING\n%s", result.Status, result.FormatTree())
	}

	open := runner.OpenCallbacks()
	if len(open) != 1 {
		t.Fatalf("open callbacks = %d, want 1", len(open))
	}
	if err := runner.SendCallbackSuccess(open[0].CallbackID, approval{Approved: true}); err != nil {
		t.Fatal(err)
	}

	result, err = runner.RunUntilComplete(nil)
	if err != nil {
		t.Fatal(err)
	}
	if result.Status != durabletest.Succeeded {
		t.Fatalf("status = %s, want SUCCEEDED\n%s", result.Status, result.FormatTree())
	}

	raw, err := durabletest.ResultAs[string](result)
	if err != nil {
		t.Fatal(err)
	}
	var out approval
	if err := json.Unmarshal([]byte(raw), &out); err != nil {
		t.Fatal(err)
	}
	if !out.Approved {
		t.Error("approved = false, want true")
	}
}
