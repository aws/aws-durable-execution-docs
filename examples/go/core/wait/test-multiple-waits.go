package main

import (
	"testing"
	"time"

	"github.com/aws/aws-durable-execution-sdk-go/durable"
	"github.com/aws/aws-durable-execution-sdk-go/durable/durabletest"
)

type waitResult struct {
	CompletedWaits int    `json:"completedWaits"`
	FinalStep      string `json:"finalStep"`
}

func multipleWaitsHandler(ctx durable.Context, event map[string]any) (waitResult, error) {
	if err := durable.Wait(ctx, "wait-1", 5*time.Second); err != nil {
		return waitResult{}, err
	}
	if err := durable.Wait(ctx, "wait-2", 5*time.Second); err != nil {
		return waitResult{}, err
	}
	return waitResult{CompletedWaits: 2, FinalStep: "done"}, nil
}

func TestMultipleWaits(t *testing.T) {
	runner := durabletest.NewLocalRunner(multipleWaitsHandler)

	result, err := runner.RunUntilComplete(map[string]any{})
	if err != nil {
		t.Fatal(err)
	}
	if result.Status != durabletest.Succeeded {
		t.Fatalf("status = %s, want SUCCEEDED", result.Status)
	}

	out, err := durabletest.ResultAs[waitResult](result)
	if err != nil {
		t.Fatal(err)
	}
	if out.CompletedWaits != 2 || out.FinalStep != "done" {
		t.Fatalf("result = %+v", out)
	}

	if len(result.Operations) != 2 {
		t.Fatalf("got %d operations, want 2:\n%s", len(result.Operations), result.FormatTree())
	}

	for _, name := range []string{"wait-1", "wait-2"} {
		op := result.Operation(name)
		if op == nil {
			t.Fatalf("missing wait %q:\n%s", name, result.FormatTree())
		}
		if op.Type != "WAIT" || op.Status != "SUCCEEDED" {
			t.Fatalf("%s: type=%s status=%s", name, op.Type, op.Status)
		}
		if op.WaitDetails == nil || op.WaitDetails.WaitSeconds != 5 {
			t.Fatalf("%s: unexpected wait details %+v", name, op.WaitDetails)
		}
	}
}
