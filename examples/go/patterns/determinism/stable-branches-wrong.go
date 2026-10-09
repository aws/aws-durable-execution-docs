package main

import (
	"time"

	"github.com/aws/aws-durable-execution-sdk-go/durable"
)

type Event struct{}

func runMorning() (string, error)   { return "morning-done", nil }
func runAfternoon() (string, error) { return "afternoon-done", nil }

// WRONG: the branch reads the wall clock outside a step, so replay can take a
// different branch than the first invocation.
func handler(ctx durable.Context, event Event) (string, error) {
	if time.Now().Hour() < 12 {
		return durable.Step(ctx, "morning-work", func(durable.StepContext) (string, error) {
			return runMorning()
		})
	}
	return durable.Step(ctx, "afternoon-work", func(durable.StepContext) (string, error) {
		return runAfternoon()
	})
}

func main() {
	durable.Start(handler)
}
