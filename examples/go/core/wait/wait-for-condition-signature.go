func WaitForCondition[S any](ctx Context, name string, check func(StepContext, S) (S, error), cfg ConditionConfig[S]) (S, error)

type ConditionConfig[S any] struct {
	// InitialState is the state passed to the first check.
	InitialState S

	// WaitStrategy decides, after each check, whether to keep waiting and
	// for how long. attempt is the 1-based number of completed checks.
	// The strategy must be a deterministic function of its arguments,
	// except for randomized jitter in the returned delay.
	//
	// WaitStrategy is required and must not be nil. When it is nil,
	// [WaitForCondition] returns an error at the call and runs no check.
	WaitStrategy func(state S, attempt int) WaitDecision

	// Serdes overrides the serializer for the condition state.
	Serdes Serdes
}
