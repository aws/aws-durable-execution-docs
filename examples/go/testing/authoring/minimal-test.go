// Save this file as minimal_test.go and run go test.
package main

import (
	"testing"

	"github.com/aws/aws-durable-execution-sdk-go/durable"
	"github.com/aws/aws-durable-execution-sdk-go/durable/durabletest"
)

func greetHandler(ctx durable.Context, _ any) (string, error) {
	return durable.Step[string](ctx, "greet", func(durable.StepContext) (string, error) {
		return "hello", nil
	})
}

func TestMinimal(t *testing.T) {
	runner := durabletest.NewLocalRunner(greetHandler)

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
	if got != "hello" {
		t.Errorf("result = %q, want hello", got)
	}
}
