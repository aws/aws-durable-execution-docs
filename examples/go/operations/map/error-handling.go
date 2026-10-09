// Package main reads item failures from a Map result. Under the default
// fail-fast policy, Map returns the populated result together with a
// *durable.BatchError.
package main

import (
	"errors"
	"fmt"
	"strings"

	"github.com/aws/aws-durable-execution-sdk-go/durable"
)

type Input struct {
	Items []string `json:"items"`
}

type Output struct {
	Succeeded int      `json:"succeeded"`
	Failed    int      `json:"failed"`
	Results   []string `json:"results"`
	Errors    []string `json:"errors"`
}

func handler(ctx durable.Context, event Input) (Output, error) {
	result, err := durable.Map(ctx, "process-items", event.Items,
		func(ctx durable.Context, item string, index int) (string, error) {
			return durable.Step(ctx, fmt.Sprintf("process-%d", index),
				func(durable.StepContext) (string, error) {
					if item == "bad" {
						return "", errors.New("bad item")
					}
					return strings.ToUpper(item), nil
				})
		})
	var batchErr *durable.BatchError
	if err != nil && !errors.As(err, &batchErr) {
		return Output{}, err
	}

	messages := make([]string, 0, result.FailureCount())
	for _, e := range result.Errors() {
		messages = append(messages, e.Error())
	}
	return Output{
		Succeeded: result.SuccessCount(),
		Failed:    result.FailureCount(),
		Results:   result.Results(),
		Errors:    messages,
	}, nil
}

func main() { durable.Start(handler) }
