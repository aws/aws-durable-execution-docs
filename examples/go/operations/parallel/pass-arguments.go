// Package main builds parallel branches in a loop, capturing a per-branch
// argument in the branch closure. In a module that declares Go 1.22 or later,
// each iteration has its own loop variable, so the closure captures the value
// for its branch.
package main

import (
	"fmt"

	"github.com/aws/aws-durable-execution-sdk-go/durable"
)

func handler(ctx durable.Context, _ any) ([]string, error) {
	items := []string{"a", "b", "c"}

	branches := make([]durable.Branch[string], 0, len(items))
	for _, item := range items {
		branches = append(branches, durable.Branch[string]{
			Func: func(ctx durable.Context) (string, error) {
				return durable.Step(ctx, fmt.Sprintf("process-%s", item),
					func(durable.StepContext) (string, error) {
						return "processed " + item, nil
					})
			},
		})
	}

	result, err := durable.Parallel(ctx, "process-items", branches)
	if err != nil {
		return nil, err
	}
	return result.Results(), nil
}

func main() { durable.Start(handler) }
