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
	cb, err := durable.CreateCallback[json.RawMessage](ctx, "wait-for-approval")
	if err != nil {
		return approvalResponse{}, err
	}

	// Send the callback ID to the external system that will resume this function.
	if err := sendApprovalRequest(cb.ID(), event.RequestID); err != nil {
		return approvalResponse{}, err
	}

	// Execution suspends here until the external system calls back.
	result, err := cb.Result(ctx)
	if err != nil {
		return approvalResponse{}, err
	}
	return approvalResponse{Approved: true, Result: result}, nil
}

func sendApprovalRequest(callbackID, requestID string) error {
	return nil
}

func main() { durable.Start(handler) }
