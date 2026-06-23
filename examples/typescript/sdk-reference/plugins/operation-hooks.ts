async onOperationStart(info: OperationInfo): Promise<void> {
  console.log("operation start", info.type, info.name, info.isReplay);
},

async onOperationEnd(info: OperationEndInfo): Promise<void> {
  console.log("operation end", info.name, info.status, info.error);
},
