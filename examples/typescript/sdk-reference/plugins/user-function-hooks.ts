async onOperationAttemptStart(info: AttemptInfo): Promise<void> {
  console.log(`attempt ${info.name} start, attempt: ${info.attempt}`);
},

async onOperationAttemptEnd(info: AttemptEndInfo): Promise<void> {
  console.log(`attempt ${info.name} end, attempt: ${info.attempt}, outcome: ${info.outcome}`);
},
