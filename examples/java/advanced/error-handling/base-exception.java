try {
    // Your code here
} catch (DurableExecutionException e) {
    // Handle any SDK exception
    System.out.println("SDK error: " + e.getMessage());
}
