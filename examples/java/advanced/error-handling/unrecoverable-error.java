try {
    // Your code here
} catch (StepFailedException e) {
    // Access error details from unrecoverable errors
    System.out.println("Execution terminated: " + e.getMessage());
} catch (InvokeFailedException e) {
    System.out.println("Invocation failed: " + e.getMessage());
}
