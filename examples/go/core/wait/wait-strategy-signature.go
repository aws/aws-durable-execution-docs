type WaitStrategy[S any] func(state S, attempt int) WaitDecision

type WaitDecision struct {
	// Continue indicates whether to keep waiting and check again.
	Continue bool

	// Delay is how long to suspend before the next check. A Delay of 0 is
	// raised to one second. A positive Delay is rounded up to whole
	// seconds, so a positive Delay under one second waits one second. A
	// negative Delay makes WaitForCondition return an error. Delay is
	// ignored when Continue is false.
	Delay time.Duration

	// Err, when non-nil and Continue is false, signals that the
	// operation should fail with this error rather than succeed. Use it
	// to implement max-attempts or timeout strategies.
	Err error
}
