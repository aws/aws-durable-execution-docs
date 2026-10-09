// Save this file as test_failure_test.go and run go test.
package main

import (
	"errors"
	"strings"
	"testing"

	"github.com/aws/aws-durable-execution-sdk-go/durable"
	"github.com/aws/aws-durable-execution-sdk-go/durable/durabletest"
)

type failInput struct {
	Fail bool `json:"fail"`
}

func failHandler(ctx durable.Context, event failInput) (string, error) {
	if event.Fail {
		return "", errors.New("intentional failure")
	}
	return "ok", nil
}

func TestFailure(t *testing.T) {
	runner := durabletest.NewLocalRunner(failHandler)

	result, err := runner.RunUntilComplete(failInput{Fail: true})
	if err != nil {
		t.Fatal(err)
	}
	if result.Status != durabletest.Failed {
		t.Fatalf("status = %s, want FAILED\n%s", result.Status, result.FormatTree())
	}
	if result.Error == nil {
		t.Fatal("result.Error is nil, want an error")
	}
	if !strings.Contains(result.Error.Message, "intentional failure") {
		t.Errorf("error message = %q, want it to contain %q", result.Error.Message, "intentional failure")
	}
}
