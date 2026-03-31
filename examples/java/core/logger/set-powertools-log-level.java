// Java equivalent: configure log level via SLF4J binding (Log4j2 or Logback)

// Option 1: Log4j2 — log4j2.xml
// <Configuration>
//   <Loggers>
//     <Root level="DEBUG">
//       <AppenderRef ref="Console" />
//     </Root>
//   </Loggers>
// </Configuration>

// Option 2: Logback — logback.xml
// <configuration>
//   <root level="DEBUG">
//     <appender-ref ref="STDOUT" />
//   </root>
// </configuration>

// Option 3: Environment variable (Lambda)
// AWS_LAMBDA_LOG_LEVEL=DEBUG
