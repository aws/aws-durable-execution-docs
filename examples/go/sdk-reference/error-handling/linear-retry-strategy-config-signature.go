type LinearRetryConfig struct {
	// MaxAttempts is the maximum number of total attempts, including the
	// first. The default is 6. It must not be negative.
	MaxAttempts int

	// InitialDelay is the delay before the first retry. The default is
	// 1 second. When set, it must be at least 1 second.
	InitialDelay time.Duration

	// Increment is added to the delay before each retry after the first.
	// The default is 1 second. It must not be negative.
	Increment time.Duration

	// MaxDelay caps the delay between retries. The default is 5 minutes.
	// When set, it must be at least 1 second.
	MaxDelay time.Duration

	// Jitter is the jitter strategy applied to computed delays. The
	// default is [JitterNone], so the default sequence is exact.
	Jitter JitterStrategy

	// RetryableErrors restricts retries to errors that at least one
	// matcher reports as retryable. When empty, every error is retryable.
	// Entries must not be nil. See [RetryConfig.RetryableErrors].
	RetryableErrors []ErrorMatcher
	// Has unexported fields.
}
