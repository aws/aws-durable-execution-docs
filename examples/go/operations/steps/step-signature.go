// Sync
func Step[O any](ctx Context, name string, fn func(StepContext) (O, error), opts ...StepOption) (O, error)

// Async
func StepAsync[O any](ctx Context, name string, fn func(StepContext) (O, error), opts ...StepOption) *Future[O]
