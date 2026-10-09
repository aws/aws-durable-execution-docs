// Sync
func Wait(ctx Context, name string, d time.Duration) error

// Async
func WaitAsync(ctx Context, name string, d time.Duration) *Future[Void]
