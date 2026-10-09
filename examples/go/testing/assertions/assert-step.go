// Save this file as assert_step_test.go and run go test.
package main

import (
	"testing"

	"github.com/aws/aws-durable-execution-sdk-go/durable"
	"github.com/aws/aws-durable-execution-sdk-go/durable/durabletest"
)

func stepHandler(ctx durable.Context, _ any) (int, error) {
	return durable.Step[int](ctx, "compute", func(durable.StepContext) (int, error) {
		return 42, nil
	})
}

func TestAssertStep(t *testing.T) {
	runner := durabletest.NewLocalRunner(stepHandler)

	result, err := runner.RunUntilComplete(nil)
	if err != nil {
		t.Fatal(err)
	}
	if result.Status != durabletest.Succeeded {
		t.Fatalf("status = %s, want SUCCEEDED\n%s", result.Status, result.FormatTree())
	}

	step := result.Operation("compute")
	if step == nil {
		t.Fatalf("step \"compute\" not found\n%s", result.FormatTree())
	}
	if step.Type != "STEP" {
		t.Errorf("type = %q, want STEP", step.Type)
	}
	if step.Status != "SUCCEEDED" {
		t.Errorf("status = %q, want SUCCEEDED", step.Status)
	}

	value, err := durabletest.OperationResultAs[int](step)
	if err != nil {
		t.Fatal(err)
	}
	if value != 42 {
		t.Errorf("result = %d, want 42", value)
	}
}
