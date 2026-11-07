fn strategy() -> impl Fn(JobState, u32) -> WaitDecision {
    |state, attempt| {
        if state.status == "COMPLETED" {
            return WaitDecision::complete();
        }
        if attempt >= 10 {
            return WaitDecision::exhausted("Max attempts exceeded");
        }
        WaitDecision::continue_with(Duration::from_secs(u64::from(attempt) * 5))
    }
}
