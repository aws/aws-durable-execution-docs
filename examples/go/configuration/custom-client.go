package main

import (
	"context"
	"log"

	"github.com/aws/aws-sdk-go-v2/aws"
	"github.com/aws/aws-sdk-go-v2/config"
	lambda "github.com/aws/aws-sdk-go-v2/service/lambda"
	"github.com/aws/aws-sdk-go-v2/service/lambda/types"

	"github.com/aws/aws-durable-execution-sdk-go/durable"
)

type Response struct {
	Status string `json:"status"`
}

func handler(ctx durable.Context, _ map[string]any) (Response, error) {
	// Your durable function logic.
	return Response{Status: "success"}, nil
}

// lambdaClientAdapter implements durable.ExecutionClient over a configured
// AWS Lambda service client. durable.WithExecutionClient takes an
// ExecutionClient, whose methods use SDK-owned request and response types,
// not AWS Lambda types. The adapter maps between the two in both
// directions, so configuring the region, retry policy, or credentials of
// the client controls the calls the SDK makes.
type lambdaClientAdapter struct {
	client *lambda.Client
}

var _ durable.ExecutionClient = (*lambdaClientAdapter)(nil)

func (a *lambdaClientAdapter) GetExecutionState(ctx context.Context, in durable.GetExecutionStateInput) (durable.GetExecutionStateOutput, error) {
	wireIn := &lambda.GetDurableExecutionStateInput{
		DurableExecutionArn: aws.String(in.ExecutionArn),
		CheckpointToken:     aws.String(in.CheckpointToken),
	}
	if in.Marker != "" {
		wireIn.Marker = aws.String(in.Marker)
	}
	wireOut, err := a.client.GetDurableExecutionState(ctx, wireIn)
	if err != nil {
		return durable.GetExecutionStateOutput{}, err
	}
	return durable.GetExecutionStateOutput{
		Operations: operationsFromWire(wireOut.Operations),
		NextMarker: aws.ToString(wireOut.NextMarker),
	}, nil
}

func (a *lambdaClientAdapter) Checkpoint(ctx context.Context, in durable.CheckpointInput) (durable.CheckpointOutput, error) {
	wireIn := &lambda.CheckpointDurableExecutionInput{
		DurableExecutionArn: aws.String(in.ExecutionArn),
		CheckpointToken:     aws.String(in.CheckpointToken),
		Updates:             updatesToWire(in.Updates),
	}
	wireOut, err := a.client.CheckpointDurableExecution(ctx, wireIn)
	if err != nil {
		return durable.CheckpointOutput{}, err
	}
	out := durable.CheckpointOutput{CheckpointToken: aws.ToString(wireOut.CheckpointToken)}
	if wireOut.NewExecutionState != nil {
		out.NewExecutionState = operationsFromWire(wireOut.NewExecutionState.Operations)
	}
	return out, nil
}

func updatesToWire(updates []durable.OperationUpdate) []types.OperationUpdate {
	if updates == nil {
		return nil
	}
	wire := make([]types.OperationUpdate, 0, len(updates))
	for _, u := range updates {
		w := types.OperationUpdate{
			Id:       u.Id,
			Type:     types.OperationType(u.Type),
			Action:   types.OperationAction(u.Action),
			SubType:  u.SubType,
			Name:     u.Name,
			ParentId: u.ParentId,
			Payload:  u.Payload,
			Error:    errorObjectToWire(u.Error),
		}
		if o := u.StepOptions; o != nil {
			w.StepOptions = &types.StepOptions{NextAttemptDelaySeconds: o.NextAttemptDelaySeconds}
		}
		if o := u.WaitOptions; o != nil {
			w.WaitOptions = &types.WaitOptions{WaitSeconds: o.WaitSeconds}
		}
		if o := u.CallbackOptions; o != nil {
			w.CallbackOptions = &types.CallbackOptions{
				TimeoutSeconds:          o.TimeoutSeconds,
				HeartbeatTimeoutSeconds: o.HeartbeatTimeoutSeconds,
			}
		}
		if o := u.ChainedInvokeOptions; o != nil {
			w.ChainedInvokeOptions = &types.ChainedInvokeOptions{
				FunctionName: o.FunctionName,
				TenantId:     o.TenantId,
			}
		}
		if o := u.ContextOptions; o != nil {
			w.ContextOptions = &types.ContextOptions{ReplayChildren: o.ReplayChildren}
		}
		wire = append(wire, w)
	}
	return wire
}

func operationsFromWire(ops []types.Operation) []durable.Operation {
	if ops == nil {
		return nil
	}
	out := make([]durable.Operation, 0, len(ops))
	for _, op := range ops {
		out = append(out, operationFromWire(op))
	}
	return out
}

func operationFromWire(op types.Operation) durable.Operation {
	o := durable.Operation{
		Id:             op.Id,
		Status:         durable.OperationStatus(op.Status),
		Type:           durable.OperationType(op.Type),
		SubType:        op.SubType,
		Name:           op.Name,
		ParentId:       op.ParentId,
		StartTimestamp: op.StartTimestamp,
		EndTimestamp:   op.EndTimestamp,
	}
	if d := op.ExecutionDetails; d != nil {
		o.ExecutionDetails = &durable.ExecutionDetails{InputPayload: d.InputPayload}
	}
	if d := op.StepDetails; d != nil {
		o.StepDetails = &durable.StepDetails{
			Attempt:              d.Attempt,
			Result:               d.Result,
			Error:                errorObjectFromWire(d.Error),
			NextAttemptTimestamp: d.NextAttemptTimestamp,
		}
	}
	if d := op.WaitDetails; d != nil {
		o.WaitDetails = &durable.WaitDetails{ScheduledEndTimestamp: d.ScheduledEndTimestamp}
	}
	if d := op.CallbackDetails; d != nil {
		o.CallbackDetails = &durable.CallbackDetails{
			CallbackId: d.CallbackId,
			Result:     d.Result,
			Error:      errorObjectFromWire(d.Error),
		}
	}
	if d := op.ChainedInvokeDetails; d != nil {
		o.ChainedInvokeDetails = &durable.ChainedInvokeDetails{
			Result: d.Result,
			Error:  errorObjectFromWire(d.Error),
		}
	}
	if d := op.ContextDetails; d != nil {
		o.ContextDetails = &durable.ContextDetails{
			Result:         d.Result,
			ReplayChildren: d.ReplayChildren,
			Error:          errorObjectFromWire(d.Error),
		}
	}
	return o
}

func errorObjectToWire(e *durable.ErrorObject) *types.ErrorObject {
	if e == nil {
		return nil
	}
	return &types.ErrorObject{
		ErrorType:    e.ErrorType,
		ErrorMessage: e.ErrorMessage,
		ErrorData:    e.ErrorData,
		StackTrace:   e.StackTrace,
	}
}

func errorObjectFromWire(e *types.ErrorObject) *durable.ErrorObject {
	if e == nil {
		return nil
	}
	return &durable.ErrorObject{
		ErrorType:    e.ErrorType,
		ErrorMessage: e.ErrorMessage,
		ErrorData:    e.ErrorData,
		StackTrace:   e.StackTrace,
	}
}

func main() {
	cfg, err := config.LoadDefaultConfig(context.Background(),
		config.WithRegion("us-west-2"),
		config.WithRetryMaxAttempts(5),
		config.WithRetryMode(aws.RetryModeAdaptive),
	)
	if err != nil {
		log.Fatal(err)
	}
	client := &lambdaClientAdapter{client: lambda.NewFromConfig(cfg)}
	durable.Start(handler, durable.WithExecutionClient(client))
}
