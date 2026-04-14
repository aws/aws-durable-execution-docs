# AWS Lambda durable functions developer documentation source

This repository contains the source for the AWS Lambda Durable Execution SDK
documentation website at https://docs.aws.amazon.com/durable-execution,
providing comprehensive guides and references for building resilient,
long-running applications with AWS Lambda durable functions across
multiple programming languages.

## Repository Structure

```
docs/                                    # Documentation source files
examples/                                # Code source files for code-blocks in docs/
aws-lambda-durable-functions-power/      # Kiro power for AI assistance
```

## Documentation source

The `docs/` directory contains the Markdown source files that power the
documentation site.

Code examples are in `examples/`, organized by language
(`typescript/`, `python/`, `java/`) and mirroring the docs folder hierarchy.

Examples are embedded into documentation pages using snippet syntax — see the
[Contributing Guide](CONTRIBUTING.md) for details.

### Getting started with the docs

[Zensical](https://zensical.org) builds this site from Markdown to HTML.

To preview locally:

```bash
python -m venv ~/.venvs/zensical
source ~/.venvs/zensical/bin/activate
pip install zensical

# Live preview with hot-reload
zensical serve
```

For the full authoring workflow — adding code samples, formatting conventions,
commit messages, and pull request process — see
[CONTRIBUTING.md](CONTRIBUTING.md). For Markdown formatting syntax, please see
the [Authoring Guide](authoring-guide.md).

## Kiro Power

This repository includes a [Kiro power](https://kiro.dev/powers/) that provides
AI-assisted guidance for building durable functions. The power includes:

- Replay model rules and best practices
- Step operation patterns
- Error handling strategies
- Testing patterns
- Deployment templates

See [aws-lambda-durable-functions-power/](aws-lambda-durable-functions-power/)
for details.

## Resources

- [AWS Lambda durable functions Documentation](https://docs.aws.amazon.com/lambda/latest/dg/durable-functions.html)
- [JavaScript SDK Repository](https://github.com/aws/aws-durable-execution-sdk-js)
- [Python SDK Repository](https://github.com/aws/aws-durable-execution-sdk-python)

## Feedback & Support

We welcome feedback and contributions:

- **Issues**: Report documentation bugs or request improvements via
  [GitHub Issues](https://github.com/aws/aws-durable-execution-docs/issues)
- **Discussions**: Ask questions and share ideas in
  [GitHub Discussions](https://github.com/aws/aws-durable-execution-docs/discussions)
- **AWS Support**: For AWS Lambda service issues, contact
  [AWS Support](https://aws.amazon.com/support)

## Contributing

We welcome contributions! Please see [CONTRIBUTING.md](CONTRIBUTING.md)
for details on how to contribute to the documentation.

## Security

See [CONTRIBUTING](CONTRIBUTING.md#security-issue-notifications) for more
information on reporting security issues.

## License

This project is licensed under the Apache-2.0 License. See the
[LICENSE](LICENSE) file for details.
