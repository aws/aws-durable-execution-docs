package main

import (
	"time"

	"github.com/aws/aws-durable-execution-sdk-go/durable"
	"github.com/aws/aws-durable-execution-sdk-go/telemetry"
)

func handler(ctx durable.Context, event map[string]string) (string, error) {
	data, err := durable.Step(ctx, "fetch-data", func(sc durable.StepContext) (string, error) {
		return event["id"], nil
	})
	if err != nil {
		return "", err
	}

	if err := durable.Wait(ctx, "cooldown", 5*time.Second); err != nil {
		return "", err
	}

	return durable.Step(ctx, "process", func(sc durable.StepContext) (string, error) {
		return "processed " + data, nil
	})
}

func main() {
	plugin := telemetry.New(telemetry.Config{})
	durable.Start(handler, durable.WithPlugins(plugin.Plugin()))
}
