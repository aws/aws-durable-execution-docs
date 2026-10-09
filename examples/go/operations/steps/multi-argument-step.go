package main

import (
	"fmt"

	"github.com/aws/aws-durable-execution-sdk-go/durable"
)

func myStep(arg1 string, arg2 int) (string, error) {
	return fmt.Sprintf("%s: %d", arg1, arg2), nil
}

func handler(ctx durable.Context, event map[string]any) (string, error) {
	// Capture arguments in the closure. The step body takes only StepContext.
	arg1, arg2 := "value", 42
	return durable.Step(ctx, "my_step", func(sc durable.StepContext) (string, error) {
		return myStep(arg1, arg2)
	})
}

func main() {
	durable.Start(handler)
}
