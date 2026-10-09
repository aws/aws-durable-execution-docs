// Save this file as long_waits_test.go and run go test.
package main

import (
	"testing"
	"time"

	"github.com/aws/aws-durable-execution-sdk-go/durable"
	"github.com/aws/aws-durable-execution-sdk-go/durable/durabletest"
)

func longWaitHandler(ctx durable.Context, _ any) (string, error) {
	if err := durable.Wait(ctx, "cooling-off", 24*time.Hour); err != nil {
		return "", err
	}
	return durable.Step[string](ctx, "after-wait", func(durable.StepContext) (string, error) {
		return "done", nil
	})
}

func TestLongWaits(t *testing.T) {
	runner := durabletest.NewLocalRunner(longWaitHandler)

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

	wait := result.Operation("cooling-off")
	if wait == nil {
		t.Fatalf("wait \"cooling-off\" not found\n%s", result.FormatTree())
	}
	if wait.Type != "WAIT" {
		t.Errorf("type = %q, want WAIT", wait.Type)
	}
	if wait.WaitDetails.WaitSeconds != 86400 {
		t.Errorf("waitSeconds = %d, want 86400", wait.WaitDetails.WaitSeconds)
	}
}
