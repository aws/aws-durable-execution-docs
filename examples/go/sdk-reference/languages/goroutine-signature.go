func Go[O any](ctx Context, name string, fn func(Context) (O, error), opts ...ChildOption) *Future[O]
func (f *Future[O]) Result(ctx Context) (O, error)
