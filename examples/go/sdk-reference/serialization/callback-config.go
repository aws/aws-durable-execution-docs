package main

import (
	"github.com/aws/aws-durable-execution-sdk-go/durable"
)

type ApprovalResult struct {
	Approved bool   `json:"approved"`
	Reason   string `json:"reason"`
}

func handler(ctx durable.Context, _ any) (ApprovalResult, error) {
	// JSONSerdes decodes the payload the external system sends into the struct.
	approval, err := durable.CreateCallback[ApprovalResult](ctx, "await-approval",
		durable.WithCallbackSerdes(durable.JSONSerdes))
	if err != nil {
		return ApprovalResult{}, err
	}

	// Send approval.ID() to the external system here.
	ctx.Logger().Info("Callback ID", "callbackId", approval.ID())

	return approval.Result(ctx)
}

func main() { durable.Start(handler) }
