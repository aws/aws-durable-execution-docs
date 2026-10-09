package main

import "github.com/aws/aws-durable-execution-sdk-go/durable"

type Event struct {
	Key string `json:"key"`
}

func getObject(bucket, key string) (string, error) { return "contents-of-" + key, nil }

func summarize(data string) (string, error) { return "summary-of-" + data, nil }

// Wrong: the step returns the full document, so the checkpoint stores it.
func handler(ctx durable.Context, event Event) (string, error) {
	document, err := durable.Step(ctx, "fetch-document", func(durable.StepContext) (string, error) {
		return getObject("docs", event.Key)
	})
	if err != nil {
		return "", err
	}

	return durable.Step(ctx, "summarize", func(durable.StepContext) (string, error) {
		return summarize(document)
	})
}

func main() {
	durable.Start(handler)
}
