package main

import (
	"fmt"
	"time"

	"github.com/aws/aws-durable-execution-sdk-go/durable"
)

func doThing() (string, error) { return "done", nil }

// Wrong: the name changes on every invocation, so replay cannot match the step.
func handler(ctx durable.Context, _ any) (string, error) {
	return durable.Step(ctx, fmt.Sprintf("run-%d", time.Now().UnixMilli()), func(durable.StepContext) (string, error) {
		return doThing()
	})
}

func main() {
	durable.Start(handler)
}
