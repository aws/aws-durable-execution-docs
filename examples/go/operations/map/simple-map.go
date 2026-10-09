// Package main squares each number in a collection with Map and returns the
// results in input order.
package main

import (
	"fmt"

	"github.com/aws/aws-durable-execution-sdk-go/durable"
)

func handler(ctx durable.Context, _ any) ([]int, error) {
	result, err := durable.Map(ctx, "square-numbers", []int{1, 2, 3, 4, 5},
		func(ctx durable.Context, item int, index int) (int, error) {
			return durable.Step(ctx, fmt.Sprintf("square-%d", index),
				func(durable.StepContext) (int, error) {
					return item * item, nil
				})
		})
	if err != nil {
		return nil, err
	}
	return result.Results(), nil
}

func main() { durable.Start(handler) }
