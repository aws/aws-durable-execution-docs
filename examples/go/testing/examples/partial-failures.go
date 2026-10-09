// Save this file as partial_failures_test.go and run go test.
package main

import (
	"errors"
	"strings"
	"testing"

	"github.com/aws/aws-durable-execution-sdk-go/durable"
	"github.com/aws/aws-durable-execution-sdk-go/durable/durabletest"
)

func partialFailureHandler(ctx durable.Context, _ any) (string, error) {
	if _, err := durable.Step[string](ctx, "step-1", func(durable.StepContext) (string, error) {
		return "ok", nil
	}); err != nil {
		return "", err
	}
	if _, err := durable.Step[string](ctx, "step-2", func(durable.StepContext) (string, error) {
		return "ok", nil
	}); err != nil {
		return "", err
	}
	return durable.Step[string](ctx, "step-3", func(durable.StepContext) (string, error) {
		return "", errors.New("step-3 failed")
	})
}

func TestPartialFailures(t *testing.T) {
	runner := durabletest.NewLocalRunner(partialFailureHandler)

	result, err := runner.RunUntilComplete(nil)
	if err != nil {
		t.Fatal(err)
	}
	if result.Status != durabletest.Failed {
		t.Fatalf("status = %s, want FAILED\n%s", result.Status, result.FormatTree())
	}

	for _, name := range []string{"step-1", "step-2"} {
		op := result.Operation(name)
		if op == nil {
			t.Fatalf("step %q not found\n%s", name, result.FormatTree())
		}
		if op.Status != "SUCCEEDED" {
			t.Errorf("step %q status = %q, want SUCCEEDED", name, op.Status)
		}
	}

	if result.Error == nil || !strings.Contains(result.Error.Message, "step-3 failed") {
		t.Errorf("error = %+v, want message containing %q", result.Error, "step-3 failed")
	}
}
