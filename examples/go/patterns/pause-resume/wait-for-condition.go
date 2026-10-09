package main

import (
	"time"

	"github.com/aws/aws-durable-execution-sdk-go/durable"
)

type Event struct {
	JobID string `json:"jobId"`
}

type JobState struct {
	JobID  string `json:"jobId"`
	Status string `json:"status"`
}

func getJobStatus(jobID string) (string, error) { return "completed", nil }

func handler(ctx durable.Context, event Event) (JobState, error) {
	check := func(_ durable.StepContext, state JobState) (JobState, error) {
		status, err := getJobStatus(state.JobID)
		if err != nil {
			return state, err
		}
		state.Status = status
		return state, nil
	}

	return durable.WaitForCondition(ctx, "wait-for-job", check, durable.ConditionConfig[JobState]{
		InitialState: JobState{JobID: event.JobID, Status: "pending"},
		WaitStrategy: func(state JobState, attempt int) durable.WaitDecision {
			if state.Status == "completed" {
				return durable.WaitDecision{Continue: false}
			}
			delay := time.Duration(1<<attempt) * time.Second
			if delay > 60*time.Second {
				delay = 60 * time.Second
			}
			return durable.WaitDecision{Continue: true, Delay: delay}
		},
	})
}

func main() {
	durable.Start(handler)
}
