package main

import (
	"encoding/json"

	"github.com/aws/aws-durable-execution-sdk-go/durable"
)

type approvalRequest struct {
	RequestID string `json:"requestId"`
}

type approvalResponse struct {
	Approved bool            `json:"approved"`
	Result   json.RawMessage `json:"result"`
}

func handler(ctx durable.Context, event approvalRequest) (approvalResponse, error) {
	result, err := durable.WaitForCallback[json.RawMessage](ctx, "wait-for-approval",
		func(_ durable.StepContext, callbackID string) error {
			return sendApprovalRequest(callbackID, event.RequestID)
		},
	)
	if err != nil {
		return approvalResponse{}, err
	}
	return approvalResponse{Approved: true, Result: result}, nil
}

func sendApprovalRequest(callbackID, requestID string) error {
	return nil
}

func main() { durable.Start(handler) }
