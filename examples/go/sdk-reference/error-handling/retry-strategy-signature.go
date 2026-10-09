type RetryStrategy func(RetryAttempt) RetryDecision

type RetryAttempt struct {
	// Err is the error from the attempt that just failed.
	Err error

	// Attempt is the 1-based number of the attempt that just failed,
	// inclusive of the first attempt.
	Attempt int

	// Elapsed is the time since the first attempt began. It is zero when
	// the SDK does not track it.
	Elapsed time.Duration
	// Has unexported fields.
}

type RetryDecision struct {
	// Retry indicates whether the operation should be attempted again.
	Retry bool

	// Delay is how long to wait before the next attempt. It is ignored
	// when Retry is false.
	Delay time.Duration
	// Has unexported fields.
}
