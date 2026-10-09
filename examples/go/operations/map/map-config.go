// Package main configures a Map with bounded concurrency, a failure
// tolerance, and flat nesting.
package main

import (
	"fmt"

	"github.com/aws/aws-durable-execution-sdk-go/durable"
	"github.com/aws/aws-sdk-go-v2/aws"
)

type Input struct {
	URLs []string `json:"urls"`
}

func handler(ctx durable.Context, event Input) ([]string, error) {
	result, err := durable.Map(ctx, "fetch-urls", event.URLs,
		func(ctx durable.Context, url string, index int) (string, error) {
			return durable.Step(ctx, fmt.Sprintf("fetch-%d", index),
				func(durable.StepContext) (string, error) {
					return fetch(url), nil
				})
		},
		durable.WithMaxConcurrency(5),
		durable.WithCompletion(durable.CompletionConfig{ToleratedFailureCount: aws.Int(2)}),
		durable.WithNesting(durable.NestingFlat),
	)
	if err != nil {
		return nil, err
	}
	return result.Results(), nil
}

// fetch is a deterministic stand-in for an HTTP GET.
func fetch(url string) string {
	return "body of " + url
}

func main() { durable.Start(handler) }
