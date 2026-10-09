// Wait for 30 seconds
if err := durable.Wait(ctx, "wait30", 30*time.Second); err != nil {
	return err
}

// Wait for 5 minutes
if err := durable.Wait(ctx, "wait5m", 5*time.Minute); err != nil {
	return err
}

// Wait for 2 hours
if err := durable.Wait(ctx, "wait2h", 2*time.Hour); err != nil {
	return err
}

// Wait for 1 day. The time package has no day unit.
if err := durable.Wait(ctx, "wait1d", 24*time.Hour); err != nil {
	return err
}

// Combined: 1 hour and 30 minutes
if err := durable.Wait(ctx, "wait90m", time.Hour+30*time.Minute); err != nil {
	return err
}
