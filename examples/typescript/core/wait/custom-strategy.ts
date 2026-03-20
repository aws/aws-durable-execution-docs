const strategy = (state: { status: string }, attempt: number) => {
  if (state.status === "COMPLETED") {
    return { shouldContinue: false };
  }
  if (attempt >= 10) {
    throw new Error("Max attempts exceeded");
  }
  return { shouldContinue: true, delay: { seconds: attempt * 5 } };
};
