// Save this file as test_retries_test.go and run go test.
package main

import (
	"errors"
	"testing"

	"github.com/aws/aws-durable-execution-sdk-go/durable"
	"github.com/aws/aws-durable-execution-sdk-go/durable/durabletest"
)

func retryHandler(ctx durable.Context, _ any) (string, error) {
	return durable.Step[string](ctx, "flaky", func(sc durable.StepContext) (string, error) {
		if sc.Attempt() < 3 {
			return "", errors.New("transient error")
		}
		return "done", nil
	}, durable.WithRetry(durable.MustNewRetryStrategy(durable.RetryConfig{MaxAttempts: 3})))
}

func TestRetries(t *testing.T) {
	runner := durabletest.NewLocalRunner(retryHandler)

	result, err := runner.RunUntilComplete(nil)
	if err != nil {
		t.Fatal(err)
	}
	if result.Status != durabletest.Succeeded {
		t.Fatalf("status = %s, want SUCCEEDED\n%s", result.Status, result.FormatTree())
	}

	got, err := durabletest.ResultAs[string](result)
	if err != nil {
		t.Fatal(err)
	}
	if got != "done" {
		t.Errorf("result = %q, want done", got)
	}
}
