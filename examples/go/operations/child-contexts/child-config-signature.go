func WithChildSerdes(s Serdes) ChildOption
func WithChildErrorMapper(mapper func(err *ChildContextError) error) ChildOption
func WithChildSummary[O any](fn func(result O) string) ChildOption
func WithChildSubType(subType string) ChildOption
func WithChildVirtual() ChildOption
