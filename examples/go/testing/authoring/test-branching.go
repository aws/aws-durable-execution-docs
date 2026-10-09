// Save this file as test_branching_test.go and run go test.
package main

import (
	"testing"

	"github.com/aws/aws-durable-execution-sdk-go/durable"
	"github.com/aws/aws-durable-execution-sdk-go/durable/durabletest"
)

type branchInput struct {
	Premium bool `json:"premium"`
}

func branchHandler(ctx durable.Context, event branchInput) (string, error) {
	if event.Premium {
		return durable.Step[string](ctx, "premium-path", func(durable.StepContext) (string, error) {
			return "premium", nil
		})
	}
	return durable.Step[string](ctx, "standard-path", func(durable.StepContext) (string, error) {
		return "standard", nil
	})
}

func TestBranching(t *testing.T) {
	cases := []struct {
		name  string
		input branchInput
		want  string
	}{
		{"premium path", branchInput{Premium: true}, "premium"},
		{"standard path", branchInput{Premium: false}, "standard"},
	}

	for _, tc := range cases {
		t.Run(tc.name, func(t *testing.T) {
			runner := durabletest.NewLocalRunner(branchHandler)

			result, err := runner.RunUntilComplete(tc.input)
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
			if got != tc.want {
				t.Errorf("result = %q, want %q", got, tc.want)
			}
		})
	}
}
