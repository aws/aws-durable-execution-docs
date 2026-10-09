package main

import (
	"time"

	"github.com/aws/aws-durable-execution-sdk-go/durable"
)

type Event struct{}

func runMorning() (string, error)   { return "morning-done", nil }
func runAfternoon() (string, error) { return "afternoon-done", nil }

// Right: a step checkpoints the decision, so every replay branches the same way.
func handler(ctx durable.Context, event Event) (string, error) {
	shift, err := durable.Step(ctx, "pick-shift", func(durable.StepContext) (string, error) {
		if time.Now().Hour() < 12 {
			return "morning", nil
		}
		return "afternoon", nil
	})
	if err != nil {
		return "", err
	}

	if shift == "morning" {
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
