// Package main nests a Map inside a Map. Each nested Map creates its own set
// of child contexts.
package main

import (
	"fmt"
	"strings"

	"github.com/aws/aws-durable-execution-sdk-go/durable"
)

type Region struct {
	Name  string   `json:"name"`
	Items []string `json:"items"`
}

type Input struct {
	Regions []Region `json:"regions"`
}

func handler(ctx durable.Context, event Input) ([][]string, error) {
	result, err := durable.Map(ctx, "process-regions", event.Regions,
		func(ctx durable.Context, region Region, index int) ([]string, error) {
			inner, err := durable.Map(ctx, fmt.Sprintf("process-%s", region.Name), region.Items,
				func(ctx durable.Context, item string, i int) (string, error) {
					return durable.Step(ctx, fmt.Sprintf("item-%d", i),
						func(durable.StepContext) (string, error) {
							return strings.ToUpper(item), nil
						})
				})
			if err != nil {
				return nil, err
			}
			return inner.Results(), nil
		})
	if err != nil {
		return nil, err
	}
	return result.Results(), nil
}

func main() { durable.Start(handler) }
