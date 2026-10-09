type Handler[I, O any] func(ctx Context, event I) (O, error)

func Start[I, O any](handler Handler[I, O], opts ...HandlerOption)
func Wrap[I, O any](handler Handler[I, O], opts ...HandlerOption) func(context.Context, []byte) ([]byte, error)
