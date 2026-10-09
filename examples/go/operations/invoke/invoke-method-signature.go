// Sync
func Invoke[O, I any](ctx Context, name, functionID string, input I, opts ...InvokeOption) (O, error)

// Async
func InvokeAsync[O, I any](ctx Context, name, functionID string, input I, opts ...InvokeOption) *Future[O]
