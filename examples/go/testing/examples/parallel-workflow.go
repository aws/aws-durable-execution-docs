// Save this file as parallel_workflow_test.go and run go test.
package main

import (
	"slices"
	"testing"

	"github.com/aws/aws-durable-execution-sdk-go/durable"
	"github.com/aws/aws-durable-execution-sdk-go/durable/durabletest"
)

func parallelHandler(ctx durable.Context, _ any) ([]string, error) {
	batch, err := durable.Parallel[string](ctx, "fetch-all", []durable.Branch[string]{
		{Func: func(c durable.Context) (string, error) {
			return durable.Step[string](c, "fetch-a", func(durable.StepContext) (string, error) {
				return "data-a", nil
			})
		}},
		{Func: func(c durable.Context) (string, error) {
			return durable.Step[string](c, "fetch-b", func(durable.StepContext) (string, error) {
				return "data-b", nil
			})
		}},
		{Func: func(c durable.Context) (string, error) {
			return durable.Step[string](c, "fetch-c", func(durable.StepContext) (string, error) {
				return "data-c", nil
			})
		}},
	})
	if err != nil {
		return nil, err
	}
	return batch.Results(), nil
}

func TestParallelWorkflow(t *testing.T) {
	runner := durabletest.NewLocalRunner(parallelHandler)

	result, err := runner.RunUntilComplete(nil)
	if err != nil {
		t.Fatal(err)
	}
	if result.Status != durabletest.Succeeded {
		t.Fatalf("status = %s, want SUCCEEDED\n%s", result.Status, result.FormatTree())
	}

	got, err := durabletest.ResultAs[[]string](result)
	if err != nil {
		t.Fatal(err)
	}
	if want := []string{"data-a", "data-b", "data-c"}; !slices.Equal(got, want) {
		t.Errorf("result = %v, want %v", got, want)
	}
}
