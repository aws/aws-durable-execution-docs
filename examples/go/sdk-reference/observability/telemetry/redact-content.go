package main

import (
	"encoding/json"
	"time"

	"github.com/aws/aws-durable-execution-sdk-go/durable"
	"github.com/aws/aws-durable-execution-sdk-go/telemetry"
)

type Result struct {
	OrderID string `json:"orderId"`
	Status  string `json:"status"`
}

func handler(ctx durable.Context, event map[string]string) (Result, error) {
	order, err := durable.Step(ctx, "fetch-order", func(sc durable.StepContext) (string, error) {
		return event["orderId"], nil
	})
	if err != nil {
		return Result{}, err
	}

	if err := durable.Wait(ctx, "cooldown", 5*time.Second); err != nil {
		return Result{}, err
	}

	return durable.Step(ctx, "process", func(sc durable.StepContext) (Result, error) {
		return Result{OrderID: order, Status: "processed"}, nil
	})
}

func keepOrderIDAndStatus(value string) string {
	var fields map[string]any
	if err := json.Unmarshal([]byte(value), &fields); err != nil {
		return "{}"
	}
	kept := map[string]any{}
	for _, key := range []string{"orderId", "status"} {
		if v, ok := fields[key]; ok {
			kept[key] = v
		}
	}
	out, _ := json.Marshal(kept)
	return string(out)
}

func main() {
	plugin := telemetry.New(telemetry.Config{
		SamplingRate: 0.1,
		Content: telemetry.ContentConfig{
			IncludeInput:           true,
			IncludeOutput:          true,
			IncludeOperationErrors: true,
			IncludeExecutionError:  true,
			Redactor:               keepOrderIDAndStatus,
		},
	})
	durable.Start(handler, durable.WithPlugins(plugin.Plugin()))
}
