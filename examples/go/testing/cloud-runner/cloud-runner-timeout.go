package main

import (
	"context"
	"testing"
	"time"

	"github.com/aws/aws-durable-execution-sdk-go/durable/durabletest"
	awsconfig "github.com/aws/aws-sdk-go-v2/config"
	"github.com/aws/aws-sdk-go-v2/service/lambda"
)

func TestGreeterPolling(t *testing.T) {
	cfg, err := awsconfig.LoadDefaultConfig(context.Background())
	if err != nil {
		t.Fatalf("load AWS config: %v", err)
	}

	runner := durabletest.NewCloudRunner(lambda.NewFromConfig(cfg), "MyFunction:$LATEST",
		durabletest.WithPollInterval(2*time.Second),
		durabletest.WithTimeout(60*time.Second))

	result, err := runner.Run(context.Background(), map[string]any{"name": "world"})
	if err != nil {
		t.Fatalf("run: %v", err)
	}
	if result.Status != durabletest.Succeeded {
		t.Fatalf("status = %s, want SUCCEEDED", result.Status)
	}
}
