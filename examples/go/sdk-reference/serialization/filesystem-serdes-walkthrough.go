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
	fsSerdes := durable.NewFileSystemSerdes("/mnt/s3")

	// Pass the FileSystem serdes to one step. Other operations in this handler
	// keep using the default JSON serdes.
	profile, err := durable.Step(ctx, "load-profile", func(durable.StepContext) (Profile, error) {
		return loadLargeProfile(event.CustomerID)
	}, durable.WithStepSerdes(fsSerdes))
	if err != nil {
		return Profile{}, err
	}

	return Profile{Name: profile.Name}, nil
}

func loadLargeProfile(customerID string) (Profile, error) {
	return Profile{Name: "Customer " + customerID}, nil
}

func main() { durable.Start(handler) }
