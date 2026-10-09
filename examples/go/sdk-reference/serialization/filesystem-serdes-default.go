package main

import (
	"github.com/aws/aws-durable-execution-sdk-go/durable"
)

type Event struct {
	CustomerID string `json:"customerId"`
}

type Profile struct {
	Name string `json:"name"`
}

func handler(ctx durable.Context, event Event) (Profile, error) {
	// Configure once. Every step result, child-context result, invoke result,
	// and WaitForCondition result in this handler is now stored on /mnt/s3.
	if err := durable.ConfigureSerdes(ctx, durable.SerdesConfig{
		Serdes: durable.NewFileSystemSerdes("/mnt/s3"),
	}); err != nil {
		return Profile{}, err
	}

	profile, err := durable.Step(ctx, "load-profile", func(durable.StepContext) (Profile, error) {
		return loadLargeProfile(event.CustomerID)
	})
	if err != nil {
		return Profile{}, err
	}

	return Profile{Name: profile.Name}, nil
}

func loadLargeProfile(customerID string) (Profile, error) {
	return Profile{Name: "Customer " + customerID}, nil
}

func main() { durable.Start(handler) }
