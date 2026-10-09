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

// WRONG: the step body appends to the captured slice. On replay the SDK returns
// the cached result without running the body, so the write never happens again.
func handler(ctx durable.Context, event Event) (Result, error) {
	receipts := []string{}
	for _, item := range event.Items {
		if _, err := durable.Step(ctx, fmt.Sprintf("save-%s", item.ID), func(durable.StepContext) (struct{}, error) {
			id, err := saveItem(item)
			if err != nil {
				return struct{}{}, err
			}
			receipts = append(receipts, id)
			return struct{}{}, nil
		}); err != nil {
			return Result{}, err
		}
	}
	return Result{Receipts: receipts}, nil
}

func main() {
	durable.Start(handler)
}
