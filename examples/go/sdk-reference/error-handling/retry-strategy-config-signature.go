type RetryConfig struct {
	// MaxAttempts is the maximum number of total attempts, including the
	// first. The default is 3. It must not be negative.
	MaxAttempts int

	// InitialDelay is the delay before the first retry. The default is
	// 5 seconds. When set, it must be at least 1 second.
	InitialDelay time.Duration

	// MaxDelay caps the delay between retries. The default is 5 minutes.
	// When set, it must be at least 1 second.
	MaxDelay time.Duration

	// BackoffRate multiplies the delay after each attempt. The default
	// is 2. It must be a finite value and must not be negative.
	BackoffRate float64

	// Jitter is the jitter strategy applied to computed delays. The
	// default is [JitterFull]. When set, it must be one of the defined
	// [JitterStrategy] constants.
	Jitter JitterStrategy

	// RetryableErrors restricts retries to errors that at least one
	// matcher reports as retryable. When empty, every error is retryable.
	// Entries must not be nil.
	RetryableErrors []ErrorMatcher
	// Has unexported fields.
}
