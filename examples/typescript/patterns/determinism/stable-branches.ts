// Wrong: replay may see different values of `new Date()`.
if (new Date().getHours() < 12) {
  await context.step("morning-work", async () => runMorning());
} else {
  await context.step("afternoon-work", async () => runAfternoon());
}

// Right: the SDK checkpoints the decision.
const shift = await context.step("pick-shift", async () => {
  return new Date().getHours() < 12 ? "morning" : "afternoon";
});
if (shift === "morning") {
  await context.step("morning-work", async () => runMorning());
} else {
  await context.step("afternoon-work", async () => runAfternoon());
}
