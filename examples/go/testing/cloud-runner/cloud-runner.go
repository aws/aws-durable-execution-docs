package main

import (
	"context"
	"testing"

	"github.com/aws/aws-durable-execution-sdk-go/durable/durabletest"
	awsconfig "github.com/aws/aws-sdk-go-v2/config"
	"github.com/aws/aws-sdk-go-v2/service/lambda"
)

func TestGreeterInCloud(t *testing.T) {
	cfg, err := awsconfig.LoadDefaultConfig(context.Background())
	if err != nil {
		t.Fatalf("load AWS config: %v", err)
	}

	// The function name needs a qualifier: a version, an alias, or $LATEST.
	runner := durabletest.NewCloudRunner(lambda.NewFromConfig(cfg), "MyFunction:$LATEST")

	result, err := runner.Run(context.Background(), map[string]any{"name": "world"})
	if err != nil {
		t.Fatalf("run: %v", err)
	}
	if result.Status != durabletest.Succeeded {
		t.Fatalf("status = %s, want SUCCEEDED", result.Status)
	}

	greeting, err := durabletest.ResultAs[string](result)
	if err != nil {
		t.Fatalf("result: %v", err)
	}
	if greeting != "hello world" {
		t.Fatalf("result = %q, want %q", greeting, "hello world")
	}
}
