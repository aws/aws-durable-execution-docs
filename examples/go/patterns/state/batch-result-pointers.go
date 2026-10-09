package main

import "github.com/aws/aws-durable-execution-sdk-go/durable"

type Event struct {
	Items []string `json:"items"`
}

type Result struct {
	Keys []string `json:"keys"`
}

func processItem(item string) (string, error) { return "out-" + item, nil }
func storeOutput(output string) (string, error) {
	return "s3://bucket/" + output, nil
}

func handler(ctx durable.Context, event Event) (Result, error) {
	// Each per-item step returns an S3 key, so the BatchResult carries
	// pointers rather than payloads.
	results, err := durable.Map(ctx, "process", event.Items,
		func(c durable.Context, item string, index int) (string, error) {
			return durable.Step(c, "process", func(durable.StepContext) (string, error) {
				output, err := processItem(item)
				if err != nil {
					return "", err
				}
				return storeOutput(output)
			})
		})
	if err != nil {
		return Result{}, err
	}
	return Result{Keys: results.Results()}, nil
}

func main() {
	durable.Start(handler)
}
