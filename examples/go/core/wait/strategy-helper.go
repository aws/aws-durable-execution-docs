strategy := durable.MustNewWaitStrategy(durable.WaitConfig[JobState]{
	MaxAttempts:    10,
	InitialDelay:   5 * time.Second,
	MaxDelay:       5 * time.Minute,
	BackoffRate:    2.0,
	Jitter:         durable.JitterFull,
	ShouldContinue: func(state JobState) bool { return state.Status != "COMPLETED" },
})
