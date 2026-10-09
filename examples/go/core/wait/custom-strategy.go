strategy := func(state JobState, attempt int) durable.WaitDecision {
	if state.Status == "COMPLETED" {
		return durable.WaitDecision{Continue: false}
	}
	if attempt >= 10 {
		return durable.WaitDecision{Continue: false, Err: errors.New("max attempts exceeded")}
	}
	return durable.WaitDecision{Continue: true, Delay: time.Duration(attempt) * 5 * time.Second}
}
