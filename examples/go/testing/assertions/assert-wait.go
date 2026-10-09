// Save this file as assert_wait_test.go and run go test.
package main

import (
	"testing"
	"time"

	"github.com/aws/aws-durable-execution-sdk-go/durable"
	"github.com/aws/aws-durable-execution-sdk-go/durable/durabletest"
)

func waitHandler(ctx durable.Context, _ any) (string, error) {
	if err := durable.Wait(ctx, "my-wait", 30*time.Second); err != nil {
		return "", err
	}
	return "done", nil
}

func TestAssertWait(t *testing.T) {
	runner := durabletest.NewLocalRunner(waitHandler)

	result, err := runner.RunUntilComplete(nil)
	if err != nil {
		t.Fatal(err)
	}
	if result.Status != durabletest.Succeeded {
		t.Fatalf("status = %s, want SUCCEEDED\n%s", result.Status, result.FormatTree())
	}

	wait := result.Operation("my-wait")
	if wait == nil {
		t.Fatalf("wait \"my-wait\" not found\n%s", result.FormatTree())
	}
	if wait.Type != "WAIT" {
		t.Errorf("type = %q, want WAIT", wait.Type)
	}
	if wait.Status != "SUCCEEDED" {
		t.Errorf("status = %q, want SUCCEEDED", wait.Status)
	}
	if wait.WaitDetails.WaitSeconds != 30 {
		t.Errorf("waitSeconds = %d, want 30", wait.WaitDetails.WaitSeconds)
	}
	if wait.WaitDetails.ScheduledEndTimestamp.IsZero() {
		t.Error("scheduledEndTimestamp is zero, want a scheduled end time")
	}
}
