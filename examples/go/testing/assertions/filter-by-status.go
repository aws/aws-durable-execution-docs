// Save this file as filter_by_status_test.go and run go test.
package main

import (
	"errors"
	"testing"

	"github.com/aws/aws-durable-execution-sdk-go/durable"
	"github.com/aws/aws-durable-execution-sdk-go/durable/durabletest"
)

func flakyHandler(ctx durable.Context, _ any) (string, error) {
	return durable.Step[string](ctx, "flaky", func(sc durable.StepContext) (string, error) {
		if sc.Attempt() < 3 {
			return "", errors.New("not yet")
		}
		return "ok", nil
	}, durable.WithRetry(durable.MustNewRetryStrategy(durable.RetryConfig{MaxAttempts: 3})))
}

func TestFilterByStatus(t *testing.T) {
	runner := durabletest.NewLocalRunner(flakyHandler)

	result, err := runner.RunUntilComplete(nil)
	if err != nil {
		t.Fatal(err)
	}
	if result.Status != durabletest.Succeeded {
		t.Fatalf("status = %s, want SUCCEEDED\n%s", result.Status, result.FormatTree())
	}

	byStatus := map[string]int{}
	for _, op := range result.Operations {
		byStatus[op.Status]++
	}
	if byStatus["SUCCEEDED"] != 1 {
		t.Errorf("succeeded operations = %d, want 1", byStatus["SUCCEEDED"])
	}
	if byStatus["FAILED"] != 0 {
		t.Errorf("failed operations = %d, want 0", byStatus["FAILED"])
	}

	flaky := result.Operation("flaky")
	if flaky == nil {
		t.Fatalf("step \"flaky\" not found\n%s", result.FormatTree())
	}
	if flaky.StepDetails.Attempt != 2 {
		t.Errorf("attempt = %d, want 2", flaky.StepDetails.Attempt)
	}
}
