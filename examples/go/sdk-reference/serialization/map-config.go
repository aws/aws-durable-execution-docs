package main

import (
	"context"
	"encoding/json"

	"github.com/aws/aws-durable-execution-sdk-go/durable"
)

type ProcessedItem struct {
	ID     string `json:"id"`
	Status string `json:"status"`
}

var itemSerdes = durable.SerdesOf(
	func(_ context.Context, _ durable.SerdesContext, p ProcessedItem) ([]byte, error) {
		return json.Marshal(p)
	},
	func(_ context.Context, _ durable.SerdesContext, data []byte) (ProcessedItem, error) {
		var p ProcessedItem
		err := json.Unmarshal(data, &p)
		return p, err
	},
)

func handler(ctx durable.Context, _ any) ([]ProcessedItem, error) {
	items := []string{"a", "b", "c"}
	result, err := durable.Map(ctx, "process-items", items,
		func(_ durable.Context, item string, _ int) (ProcessedItem, error) {
			return ProcessedItem{ID: item, Status: "done"}, nil
		},
		durable.WithBatchSerdes(itemSerdes),
	)
	if err != nil {
		return nil, err
	}
	return result.Results(), nil
}

func main() { durable.Start(handler) }
