func Map[I, O any](ctx Context, name string, items []I, fn func(ctx Context, item I, index int) (O, error), opts ...BatchOption) (BatchResult[O], error)
