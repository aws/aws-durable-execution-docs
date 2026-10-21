package main

import (
	"context"
	"log"
	"time"

	"github.com/aws/aws-durable-execution-sdk-go/durable"
	"github.com/aws/aws-durable-execution-sdk-go/telemetry"
	"github.com/aws/aws-sdk-go-v2/config"
	"github.com/aws/aws-sdk-go-v2/service/dynamodb"
	"github.com/aws/aws-sdk-go-v2/service/eventbridge"
	"github.com/aws/aws-sdk-go-v2/service/s3"
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
	cfg, err := config.LoadDefaultConfig(context.Background())
	if err != nil {
		log.Fatal(err)
	}

	plugin := telemetry.New(telemetry.Config{
		EmitMode:     telemetry.EmitOnComplete,
		SamplingRate: 1.0,
		Exporters: []telemetry.Exporter{
			&telemetry.S3Exporter{
				API:    s3.NewFromConfig(cfg),
				Bucket: "durable-execution-telemetry-archive",
			},
			&telemetry.DynamoDBExporter{
				API:       dynamodb.NewFromConfig(cfg),
				TableName: "durable-execution-telemetry-live",
			},
			&telemetry.EventBridgeExporter{
				API: eventbridge.NewFromConfig(cfg),
			},
		},
	})

	durable.Start(handler, durable.WithPlugins(plugin.Plugin()))
}
