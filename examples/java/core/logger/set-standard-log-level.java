// Set log level via SLF4J binding configuration

// Log4j2 — log4j2.xml:
// <Root level="DEBUG">
//   <AppenderRef ref="Console" />
// </Root>

// Or programmatically with Log4j2:
Configurator.setRootLevel(Level.DEBUG);
