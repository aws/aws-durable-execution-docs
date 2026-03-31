// Use a custom SerDes for raw JSON string handling
var config = StepConfig.builder()
    .serDes(new CustomJsonSerDes())
    .build();

var result = ctx.step("process-json", String.class,
    stepCtx -> processJson(jsonString), config);
