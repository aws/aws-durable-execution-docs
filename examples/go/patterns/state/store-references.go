package main

import "github.com/aws/aws-durable-execution-sdk-go/durable"

type Event struct {
	Key string `json:"key"`
}

type Ref struct {
	Bucket string `json:"bucket"`
	Key    string `json:"key"`
}

func getObject(bucket, key string) (string, error) { return "contents-of-" + key, nil }

func stageForProcessing(data string) (string, error) { return "staged-" + data, nil }

func summarize(data string) (string, error) { return "summary-of-" + data, nil }

// Right: only the reference flows between steps.
func handler(ctx durable.Context, event Event) (string, error) {
	ref, err := durable.Step(ctx, "stage-document", func(durable.StepContext) (Ref, error) {
		data, err := getObject("docs", event.Key)
		if err != nil {
			return Ref{}, err
		}
		stagedKey, err := stageForProcessing(data)
		if err != nil {
			return Ref{}, err
		}
		return Ref{Bucket: "processing", Key: stagedKey}, nil
	})
	if err != nil {
		return "", err
	}

	return durable.Step(ctx, "summarize", func(durable.StepContext) (string, error) {
		data, err := getObject(ref.Bucket, ref.Key)
		if err != nil {
			return "", err
		}
		return summarize(data)
	})
}

func main() {
	durable.Start(handler)
}
