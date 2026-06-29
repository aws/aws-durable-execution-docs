async onOperationAttemptStart(info: AttemptInfo): Promise<void> {
  console.log("attempt start", info.name, info.attempt);
},

async onOperationAttemptEnd(info: AttemptEndInfo): Promise<void> {
  console.log("attempt end", info.name, info.attempt, info.outcome);
},
