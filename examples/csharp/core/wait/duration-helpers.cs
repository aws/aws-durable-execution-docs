// Wait for 30 seconds
await ctx.WaitAsync(TimeSpan.FromSeconds(30), name: "wait30");

// Wait for 5 minutes
await ctx.WaitAsync(TimeSpan.FromMinutes(5), name: "wait5m");

// Wait for 2 hours
await ctx.WaitAsync(TimeSpan.FromHours(2), name: "wait2h");

// Wait for 1 day
await ctx.WaitAsync(TimeSpan.FromDays(1), name: "wait1d");
