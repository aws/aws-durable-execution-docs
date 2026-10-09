// Blocking
func RunInChildContext[O any](ctx Context, name string, fn func(Context) (O, error), opts ...ChildOption) (O, error)

// Async
func RunInChildContextAsync[O any](ctx Context, name string, fn func(Context) (O, error), opts ...ChildOption) *Future[O]

// Go is shorthand for RunInChildContextAsync.
func Go[O any](ctx Context, name string, fn func(Context) (O, error), opts ...ChildOption) *Future[O]
