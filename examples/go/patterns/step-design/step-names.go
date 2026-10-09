package main

import (
	"fmt"

	"github.com/aws/aws-durable-execution-sdk-go/durable"
)

type Item struct {
	ID string `json:"id"`
}

type Event struct {
	Items []Item `json:"items"`
}

func validateOrder() (string, error)     { return "valid", nil }
func saveItem(item Item) (string, error) { return "saved-" + item.ID, nil }

func handler(ctx durable.Context, event Event) ([]string, error) {
	// A stable, descriptive name.
	if _, err := durable.Step(ctx, "validate-order", func(durable.StepContext) (string, error) {
		return validateOrder()
	}); err != nil {
		return nil, err
	}

	// Dynamic but deterministic: the name derives from the input item ID.
	saved := []string{}
	for _, item := range event.Items {
		s, err := durable.Step(ctx, fmt.Sprintf("save-item-%s", item.ID), func(durable.StepContext) (string, error) {
			return saveItem(item)
		})
		if err != nil {
			return nil, err
		}
		saved = append(saved, s)
	}
	return saved, nil
}

func main() {
	durable.Start(handler)
}
