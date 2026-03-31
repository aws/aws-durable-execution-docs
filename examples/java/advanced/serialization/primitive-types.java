// Primitives — plain JSON via Jackson
String text = "hello";
int number = 42;
double decimalNum = 3.14;
boolean flag = true;

// Simple lists of primitives
var numbers = List.of(1, 2, 3, 4, 5);

return Map.of(
    "text", text,
    "number", number,
    "decimal", decimalNum,
    "flag", flag,
    "numbers", numbers
);
