type Serdes interface {
	Marshal(ctx context.Context, meta SerdesContext, v any) ([]byte, error)
	Unmarshal(ctx context.Context, meta SerdesContext, data []byte, v any) error
}

type SerdesContext struct {
	// OperationID is the positional ID of the operation being serialized
	// (e.g., "1", "1-2-3").
	OperationID string

	// DurableExecutionArn is the ARN of the current durable execution.
	DurableExecutionArn string
	// Has unexported fields.
}
