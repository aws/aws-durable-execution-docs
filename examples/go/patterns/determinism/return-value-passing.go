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

type Result struct {
	Receipts []string `json:"receipts"`
}

func saveItem(item Item) (string, error) {
	return "r-" + item.ID, nil
}

// Right: the step returns the receipt id. The handler appends the returned
// value, which replay rebuilds from the cached step results.
func handler(ctx durable.Context, event Event) (Result, error) {
	receipts := []string{}
	for _, item := range event.Items {
		id, err := durable.Step(ctx, fmt.Sprintf("save-%s", item.ID), func(durable.StepContext) (string, error) {
			return saveItem(item)
		})
		if err != nil {
			return Result{}, err
		}
		receipts = append(receipts, id)
	}
	return Result{Receipts: receipts}, nil
}

func main() {
	durable.Start(handler)
}
