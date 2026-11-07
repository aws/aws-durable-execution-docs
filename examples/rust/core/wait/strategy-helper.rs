fn strategy() -> WaitStrategy<JobState> {
    WaitStrategy::builder(|state: &JobState| state.status == "COMPLETED")
        .max_attempts(10)
        .initial_delay(Duration::from_secs(5))
        .max_delay(Duration::from_mins(5))
        .backoff_rate(2.0)
        .jitter(JitterStrategy::Full)
        .build()
}
