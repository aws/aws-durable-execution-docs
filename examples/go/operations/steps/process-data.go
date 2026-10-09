package main

import "github.com/aws/aws-durable-execution-sdk-go/durable"

type DataEvent struct {
	Data string `json:"data"`
}

type Processed struct {
	Processed string `json:"processed"`
	Status    string `json:"status"`
}

func processData(data string) (Processed, error) {
	return Processed{Processed: data, Status: "completed"}, nil
}

func handler(ctx durable.Context, event DataEvent) (Processed, error) {
	retry := durable.MustNewRetryStrategy(durable.RetryConfig{MaxAttempts: 3})
	return durable.Step(ctx, "process_data",
		func(sc durable.StepContext) (Processed, error) {
			return processData(event.Data)
		},
		durable.WithRetry(retry),
		durable.WithSemantics(durable.AtLeastOncePerRetry),
	)
}

func main() {
	durable.Start(handler)
}
