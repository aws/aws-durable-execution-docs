package main

import (
	"log/slog"
	"os"

	"github.com/aws/aws-durable-execution-sdk-go/durable"
)

func handler(ctx durable.Context, event map[string]any) (string, error) {
	ctx.Logger().Info("Using a custom slog handler")
	return "done", nil
}

func main() {
	logHandler := slog.NewJSONHandler(os.Stdout, &slog.HandlerOptions{Level: slog.LevelInfo})
	durable.Start(handler, durable.WithLogHandler(logHandler))
}
