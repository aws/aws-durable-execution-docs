func WaitForCallback[O any](ctx Context, name string, submitter func(ctx StepContext, callbackID string) error, opts ...WaitForCallbackOption) (O, error)
