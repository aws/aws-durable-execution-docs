// Save this file as assert_child_context_test.go and run go test.
package main

import (
	"testing"

	"github.com/aws/aws-durable-execution-sdk-go/durable"
	"github.com/aws/aws-durable-execution-sdk-go/durable/durabletest"
)

func childContextHandler(ctx durable.Context, _ any) (int, error) {
	return durable.RunInChildContext[int](ctx, "process", func(child durable.Context) (int, error) {
		return durable.Step[int](child, "compute", func(durable.StepContext) (int, error) {
			return 42, nil
		})
	})
}

func TestAssertChildContext(t *testing.T) {
	runner := durabletest.NewLocalRunner(childContextHandler)

	result, err := runner.RunUntilComplete(nil)
	if err != nil {
		t.Fatal(err)
	}
	if result.Status != durabletest.Succeeded {
		t.Fatalf("status = %s, want SUCCEEDED\n%s", result.Status, result.FormatTree())
	}

	ctxOp := result.Operation("process")
	if ctxOp == nil {
		t.Fatalf("context \"process\" not found\n%s", result.FormatTree())
	}
	if ctxOp.Type != "CONTEXT" {
		t.Errorf("type = %q, want CONTEXT", ctxOp.Type)
	}
	if ctxOp.ContextDetails.Result != "42" {
		t.Errorf("context result = %q, want 42", ctxOp.ContextDetails.Result)
	}

	var children []string
	for _, op := range result.Operations {
		if op.ParentID == ctxOp.ID {
			children = append(children, op.Name)
		}
	}
	if len(children) != 1 || children[0] != "compute" {
		t.Errorf("child operations = %v, want [compute]", children)
	}

	value, err := durabletest.ResultAs[int](result)
	if err != nil {
		t.Fatal(err)
	}
	if value != 42 {
		t.Errorf("result = %d, want 42", value)
	}
}
