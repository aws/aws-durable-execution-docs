package main

import (
	"time"

	"github.com/aws/aws-durable-execution-sdk-go/durable"
)

type JobState struct {
	JobID  string `json:"jobId"`
	Status string `json:"status"`
	Done   bool   `json:"done"`
}

// getJobStatus stands in for a call to the job service. It reports the job
// complete on the second check.
func getJobStatus(sc durable.StepContext, jobID string) (string, error) {
	if sc.Attempt() < 2 {
		return "RUNNING", nil
	}
	return "COMPLETED", nil
}

func handler(ctx durable.Context, event map[string]any) (JobState, error) {
	// Poll until the job completes.
	return durable.WaitForCondition(ctx, "wait_for_job",
		func(sc durable.StepContext, state JobState) (JobState, error) {
			status, err := getJobStatus(sc, state.JobID)
			if err != nil {
				return state, err
			}
			state.Status = status
			state.Done = status == "COMPLETED"
			return state, nil
		},
		durable.ConditionConfig[JobState]{
			InitialState: JobState{JobID: "job-123", Status: "pending", Done: false},
			WaitStrategy: func(state JobState, attempt int) durable.WaitDecision {
				if state.Done {
					return durable.WaitDecision{Continue: false}
				}
				return durable.WaitDecision{
					Continue: true,
					Delay:    time.Duration(min(5*(1<<attempt), 300)) * time.Second,
				}
			},
		},
	)
}

func main() {
	durable.Start(handler)
}
