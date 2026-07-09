async onOperationStart(info: OperationInfo): Promise<void> {
  console.log(`operation ${info.name} start, type: ${info.type}, replay: ${info.isReplay}`);
},

async onOperationEnd(info: OperationEndInfo): Promise<void> {
  console.log(`operation ${info.name} end, status: ${info.status}, error: ${info.error}`);
},
