// Package main names a Map operation.
package main

import (
	"fmt"

	"github.com/aws/aws-durable-execution-sdk-go/durable"
)

type Input struct {
	UserIDs []string `json:"userIds"`
}

func handler(ctx durable.Context, event Input) ([]string, error) {
	result, err := durable.Map(ctx, "process-users", event.UserIDs,
		func(ctx durable.Context, userID string, index int) (string, error) {
			return durable.Step(ctx, fmt.Sprintf("process-%d", index),
				func(durable.StepContext) (string, error) {
					return "processed-" + userID, nil
				})
		})
	if err != nil {
		return nil, err
	}
	return result.Results(), nil
}

func main() { durable.Start(handler) }
