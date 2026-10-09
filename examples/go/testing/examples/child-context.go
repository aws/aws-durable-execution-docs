// Save this file as child_context_test.go and run go test.
package main

import (
	"slices"
	"testing"

	"github.com/aws/aws-durable-execution-sdk-go/durable"
	"github.com/aws/aws-durable-execution-sdk-go/durable/durabletest"
)

func childWorkflowHandler(ctx durable.Context, _ any) (string, error) {
	return durable.RunInChildContext[string](ctx, "process", func(child durable.Context) (string, error) {
		a, err := durable.Step[string](child, "step-a", func(durable.StepContext) (string, error) {
			return "result-a", nil
		})
		if err != nil {
			return "", err
		}
		b, err := durable.Step[string](child, "step-b", func(durable.StepContext) (string, error) {
			return "result-b", nil
		})
		if err != nil {
			return "", err
		}
		return a + ":" + b, nil
	})
}

func TestChildContext(t *testing.T) {
	runner := durabletest.NewLocalRunner(childWorkflowHandler)

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
	if got != "result-a:result-b" {
		t.Errorf("result = %q, want result-a:result-b", got)
	}

	ctxOp := result.Operation("process")
	if ctxOp == nil {
		t.Fatalf("context \"process\" not found\n%s", result.FormatTree())
	}
	if ctxOp.Type != "CONTEXT" {
		t.Errorf("type = %q, want CONTEXT", ctxOp.Type)
	}

	var children []string
	for _, op := range result.Operations {
		if op.ParentID == ctxOp.ID {
			children = append(children, op.Name)
		}
	}
	if want := []string{"step-a", "step-b"}; !slices.Equal(children, want) {
		t.Errorf("child operations = %v, want %v", children, want)
	}
}
