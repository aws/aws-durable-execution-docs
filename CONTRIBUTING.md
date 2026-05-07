# Contributing Guidelines

## Dependencies

This project uses [Zensical](https://zensical.org) to build and serve the documentation site. Set up a dedicated Python virtual environment to keep things isolated:

```bash
python -m venv ~/.venvs/zensical
source ~/.venvs/zensical/bin/activate
pip install zensical
```
### Preview site locally
Once installed, activate the venv and run Zensical from the repository root:

```bash
source ~/.venvs/zensical/bin/activate

# Live preview with hot-reload
zensical serve

# Production build
zensical build --clean
```

## Vendored dependencies

The Content Security Policy on `docs.aws.amazon.com` blocks Zensical's default
CDN load of third-party assets from `unpkg.com`, so we self-host the ones the
theme needs. Each vendored package has a matching `scripts/vendor_<name>.py`
script and a pin file (`scripts/vendor_<name>.toml`) holding the version and
expected SHA-256.

### Mermaid

The file `docs/assets/javascripts/mermaid.tiny.js` is a pinned copy of the
`@mermaid-js/tiny` UMD build.

Mermaid's tiny build supports flowcharts and sequence, state, class, and
entity-relationship diagrams. It does not support mindmap or architecture
diagrams, or KaTeX math rendering.

Upgrade procedure:

```bash
# 1. Show the pinned version and the latest on npm
python3 scripts/vendor_mermaid.py --latest

# 2. Edit scripts/vendor_mermaid.toml. Bump `version`. Run the script.
#    It will fail and print the new SHA-256. Paste that value into `sha256`
#    in the TOML, then run the script again.
python3 scripts/vendor_mermaid.py

# 3. Preview locally and spot-check pages with diagrams.
zensical serve

# 4. Commit scripts/vendor_mermaid.toml and docs/assets/javascripts/mermaid.tiny.js
#    together in the same commit.
```

### GLightbox

The files `docs/assets/javascripts/glightbox.min.js` and
`docs/assets/stylesheets/glightbox.min.css` are pinned copies of the
`glightbox` UMD build and its stylesheet. Zensical's theme bundle only
fetches glightbox from unpkg when `window.GLightbox` is undefined, so
loading the vendored UMD (via `extra_javascript` in `zensical.toml`) before
the theme subscribes prevents the third-party fetch. The matching CSS is
loaded via `extra_css`.

Upgrade procedure:

```bash
# 1. Show the pinned version and the latest on npm
python3 scripts/vendor_glightbox.py --latest

# 2. Edit scripts/vendor_glightbox.toml. Bump `version`. Run the script.
#    It will fail and print the new SHA-256 values. Paste them into
#    `js_sha256` and `css_sha256` in the TOML, then run the script again.
python3 scripts/vendor_glightbox.py

# 3. Preview locally and click an image to confirm the lightbox opens.
zensical serve

# 4. Commit scripts/vendor_glightbox.toml together with the vendored files
#    under docs/assets/javascripts/ and docs/assets/stylesheets/.
```

CI verifies each committed vendored file matches its pinned SHA-256. If you
hand-edit a vendored file or forget to update the SHA on upgrade, the build
fails.

### Directory convention

Assets under `docs/` are split by ownership:

- `docs/assets/javascripts/` and `docs/assets/stylesheets/` hold vendored
  third-party bundles. Treat these as read-only outputs of the vendoring
  scripts under `scripts/`. Do not hand-edit them. Upgrade by bumping the
  version pin in the corresponding script and re-running it. Files in these
  directories are marked `binary` in `.gitattributes` so PR diffs do not try
  to render minified code.
- `docs/javascripts/` and `docs/stylesheets/` hold first-party scripts and
  styles we author and maintain, such as `mermaid-init.js` (which initializes
  the vendored Mermaid build) and `extra.css`.

Both sets of directories are served from the same origin and register through
`extra_javascript` / `extra_css` in `zensical.toml`.

## Formatting

Run `mdformat` to auto-format Markdown files before committing:

```bash
# single file
mdformat docs/index.md

# all docs
mdformat docs/
```

## Authoring
The copy is written in Markdown and rendered by Zensical.
Please see the [authoring-guide](authoring-guide.md) for samples on how
to create code-blocks, examples, info boxes and other formatting features.

Edit documentation in the markdown files under `docs/`.

### Adding Code Sample Blocks

Embed code samples in documentation pages with the `--8<--` snippet syntax with content tabs for multi-language support.

**IMPORTANT**: All code examples MUST include all three languages (Python, TypeScript, Java) and remain functionally equivalent across languages. The tab order must always be TypeScript → Python → Java.

#### Steps to add a new code sample:

1. Create example files under `examples/` following the page folder hierarchy
2. Use identical names with hyphens across all languages (e.g., `retry-with-backoff.{py,ts,java}`)
3. Organize by language: `examples/{language}/{section}/{subsection}/{example-name}.{ext}`
4. Ensure all three language versions demonstrate the same functionality
5. Reference the examples in your documentation using content tabs:

```markdown
=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/core/steps/basic-step.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/core/steps/basic-step.py"
    ```

=== "Java"

    ```java
    --8<-- "examples/java/core/steps/basic-step.java"
    ```
```

#### Example structure:
```
examples/
├── typescript/
│   ├── getting-started/
│   │   └── minimal-example.ts
│   ├── core/
│   │   ├── steps/
│   │   │   └── basic-step.ts
│   │   └── parallel/
│   │       └── parallel-execution.ts
│   └── advanced/
│       └── error-handling/
│           └── retry-with-backoff.ts
├── python/
│   ├── getting-started/
│   │   └── minimal-example.py
│   ├── core/
│   │   ├── steps/
│   │   │   └── basic-step.py
│   │   └── parallel/
│   │       └── parallel-execution.py
│   └── advanced/
│       └── error-handling/
│           └── retry-with-backoff.py
└── java/
    ├── getting-started/
    │   └── minimal-example.java
    ├── core/
    │   ├── steps/
    │   │   └── basic-step.java
    │   └── parallel/
    │       └── parallel-execution.java
    └── advanced/
        └── error-handling/
            └── retry-with-backoff.java
```

This approach keeps code samples maintainable, testable, and consistent across all languages.

### Commit Messages

Use [Conventional Commits](https://www.conventionalcommits.org/) format:

```
<type>(<scope>): <subject>

<body>
```

- type and scope are optional
- Subject line must be 50 characters or less
- Use lowercase for type and scope
- Use imperative mood in the subject ("add" not "added" or "adds")
- No period at the end of the subject line
- Wrap body text at 72 characters
- Use the body to explain what changed and why, with bullet points when helpful

Common types: `feat`, `fix`, `docs`, `style`, `refactor`, `test`, `chore`

Example:

```
add custom serdes examples

- Add TypeScript, Python, and Java examples for custom
  serialization
- Include encryption-at-rest pattern for sensitive data
- Update serialization doc page with snippet references
```

## Developer Workflow

1. Create a feature branch and edit the Markdown files under `docs/` (and corresponding code samples under `examples/`).
2. Preview your changes locally by running `zensical serve` and opening the local URL it prints.
3. Once you're happy with the result, commit your changes and open a pull request against the *main* branch.

## Reporting Bugs/Feature Requests

We welcome you to use the GitHub issue tracker to report bugs or suggest features.

When filing an issue, please check existing open, or recently closed, issues to make sure somebody else hasn't already
reported the issue. Please try to include as much information as you can. Details like these are incredibly useful:

* A reproducible test case or series of steps
* The version of our code being used
* Any modifications you've made relevant to the bug
* Anything unusual about your environment or deployment


## Contributing via Pull Requests
Contributions via pull requests are much appreciated. Before sending us a pull request, please ensure that:

1. You are working against the latest source on the *main* branch.
2. You check existing open, and recently merged, pull requests to make sure someone else hasn't addressed the problem already.
3. You open an issue to discuss any significant work - we would hate for your time to be wasted.

To send us a pull request, please:

1. Fork the repository.
2. Modify the source; please focus on the specific change you are contributing. If you also reformat all the code, it will be hard for us to focus on your change.
3. Ensure local tests pass.
4. Commit to your fork using clear commit messages.
5. Send us a pull request, answering any default questions in the pull request interface.
6. Pay attention to any automated CI failures reported in the pull request, and stay involved in the conversation.

GitHub provides additional document on [forking a repository](https://help.github.com/articles/fork-a-repo/) and
[creating a pull request](https://help.github.com/articles/creating-a-pull-request/).


## Finding contributions to work on
Looking at the existing issues is a great way to find something to contribute on. As our projects, by default, use the default GitHub issue labels (enhancement/bug/duplicate/help wanted/invalid/question/wontfix), looking at any 'help wanted' issues is a great place to start.


## Code of Conduct
This project has adopted the [Amazon Open Source Code of Conduct](https://aws.github.io/code-of-conduct).
For more information see the [Code of Conduct FAQ](https://aws.github.io/code-of-conduct-faq) or contact
opensource-codeofconduct@amazon.com with any additional questions or comments.


## Security issue notifications
If you discover a potential security issue in this project we ask that you notify AWS/Amazon Security via our [vulnerability reporting page](http://aws.amazon.com/security/vulnerability-reporting/). Please do **not** create a public github issue.


## Licensing

See the [LICENSE](LICENSE) file for our project's licensing. We will ask you to confirm the licensing of your contribution.
