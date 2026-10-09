package main

import (
	"testing"

	"github.com/aws/aws-durable-execution-sdk-go/durable/durabletest"
)

func TestProcessOrder(t *testing.T) {
	runner := durabletest.NewLocalRunner(handler)
	result, err := runner.RunUntilComplete(order{OrderID: "order-1"})
	if err != nil {
		t.Fatal(err)
	}
	if result.Status != durabletest.Succeeded {
		t.Fatalf("status = %s, want SUCCEEDED", result.Status)
	}

	// The handler returns the child context's result.
	out, err := durabletest.ResultAs[chargedOrder](result)
	if err != nil {
		t.Fatal(err)
	}
	want := chargedOrder{OrderID: "order-1", Valid: true, Charged: true}
	if out != want {
		t.Errorf("result = %+v, want %+v", out, want)
	}

	// The SDK records a CONTEXT operation.
	contextOp := result.OperationByIndex(0)
	if contextOp == nil || contextOp.Type != "CONTEXT" || contextOp.Status != "SUCCEEDED" {
		t.Fatalf("operation 0 = %+v, want a SUCCEEDED CONTEXT operation", contextOp)
	}

	// The child operations are nested under the context operation.
	children := 0
	for _, op := range result.Operations {
		if op.ParentID == contextOp.ID {
			children++
		}
	}
	if children != 2 {
		t.Errorf("child operations = %d, want 2", children)
	}
}
