package main

import "github.com/aws/aws-durable-execution-sdk-go/durable"

type Event struct {
	Items []string `json:"items"`
}

type Result struct {
	FX        string   `json:"fx"`
	Weather   string   `json:"weather"`
	Quote     string   `json:"quote"`
	Processed []string `json:"processed"`
}

func latestFX() (string, error)           { return "1.10", nil }
func getWeather() (string, error)         { return "sunny", nil }
func getQuote() (string, error)           { return "42", nil }
func process(item string) (string, error) { return "processed-" + item, nil }

func handler(ctx durable.Context, event Event) (Result, error) {
	// A small, fixed set of branches.
	enrich, err := durable.Parallel(ctx, "enrich", []durable.Branch[string]{
		{Name: "fx", Func: func(c durable.Context) (string, error) {
			return durable.Step(c, "fx", func(durable.StepContext) (string, error) { return latestFX() })
		}},
		{Name: "weather", Func: func(c durable.Context) (string, error) {
			return durable.Step(c, "weather", func(durable.StepContext) (string, error) { return getWeather() })
		}},
		{Name: "quote", Func: func(c durable.Context) (string, error) {
			return durable.Step(c, "quote", func(durable.StepContext) (string, error) { return getQuote() })
		}},
	})
	if err != nil {
		return Result{}, err
	}

	// A variable-length list, run with a concurrency limit.
	processed, err := durable.Map(ctx, "process", event.Items,
		func(c durable.Context, item string, index int) (string, error) {
			return durable.Step(c, "process", func(durable.StepContext) (string, error) { return process(item) })
		},
		durable.WithMaxConcurrency(10),
	)
	if err != nil {
		return Result{}, err
	}

	fx, _ := enrich.Result("fx")
	weather, _ := enrich.Result("weather")
	quote, _ := enrich.Result("quote")
	return Result{FX: fx, Weather: weather, Quote: quote, Processed: processed.Results()}, nil
}

func main() {
	durable.Start(handler)
}
