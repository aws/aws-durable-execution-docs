type JitterStrategy string

const (
	// JitterFull randomizes the delay between zero and the computed
	// delay. This is the default.
	JitterFull JitterStrategy = "FULL"

	// JitterHalf randomizes the delay between half the computed delay
	// and the computed delay.
	JitterHalf JitterStrategy = "HALF"

	// JitterNone applies the computed delay unchanged.
	JitterNone JitterStrategy = "NONE"
)
