package main

import (
	"context"
	"encoding/json"

	"github.com/aws/aws-durable-execution-sdk-go/durable"
)

type Order struct {
	ID    string `json:"id"`
	Total string `json:"total"`
}

// orderSerdes implements durable.Serdes for Order results.
type orderSerdes struct{}

func (orderSerdes) Marshal(_ context.Context, _ durable.SerdesContext, v any) ([]byte, error) {
	return json.Marshal(v)
}

func (orderSerdes) Unmarshal(_ context.Context, _ durable.SerdesContext, data []byte, v any) error {
	return json.Unmarshal(data, v)
}
