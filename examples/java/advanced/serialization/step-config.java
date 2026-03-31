var config = StepConfig.builder()
    .serDes(new CustomSerDes())
    .build();

var result = ctx.step("my-step", Map.class, stepCtx -> doWork(), config);
