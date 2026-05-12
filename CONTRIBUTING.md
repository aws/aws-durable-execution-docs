# Contributing Guidelines

## Dependencies

This project uses [Zensical](https://zensical.org) to build and serve the documentation site. Set up a dedicated Python virtual environment to keep things isolated:

```bash
python -m venv ~/.venvs/zensical
source ~/.venvs/zensical/bin/activate
pip install zensical
```
### Running Zensical

Once installed, either activate the venv or run the binary directly:

```bash
# Option 1: Activate the venv
source ~/.venvs/zensical/bin/activate
zensical serve

# Option 2: Run directly without activating
~/.venvs/zensical/bin/zensical serve
```

Common commands:

- `zensical serve` — live preview with hot-reload
- `zensical build --clean` — production build
- Review build output for broken link warnings

### Adding New Pages

1. Create the markdown file in `docs/`
2. Add the page to the nav section in `zensical.toml`
3. Create corresponding code examples in all three languages under `examples/`
4. Test locally with `zensical serve`

## Vendored dependencies

The Zensical theme lazy-loads a few third-party assets (Mermaid for diagrams,
GLightbox for image zoom) from `unpkg.com` by default.

For the Content Security Policy on `docs.aws.amazon.com`, we self-host pinned
copies under `docs/assets/`. Each vendored asset exposes a `window.*` global
(`window.mermaid`, `window.GLightbox`) that the theme's lazy-loaders check
before hitting the CDN, so preloading our local copy short-circuits the
unpkg fetch.

### Mermaid

`docs/assets/javascripts/mermaid.tiny.js` is a pinned copy of the
`@mermaid-js/tiny` UMD build. The tiny build supports flowcharts and
sequence, state, class, and entity-relationship diagrams. It does not
support mindmap or architecture diagrams, or KaTeX math rendering.

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

`docs/assets/javascripts/glightbox.min.js` and
`docs/assets/stylesheets/glightbox.min.css` are pinned copies of the
`glightbox` npm package. Zensical's GLightbox Markdown extension wraps
images in `<a class="glightbox">` and, when a user opens one, calls a
lazy-loader that pulls the JS and CSS from unpkg unless
`window.GLightbox` is already defined. We register the vendored copies
via `extra_javascript` and `extra_css` in `zensical.toml` so the
lazy-loader short-circuits and no unpkg request is made.

Upgrade procedure:

```bash
# 1. Show the pinned version and the latest on npm
python3 scripts/vendor_glightbox.py --latest

# 2. Edit scripts/vendor_glightbox.toml. Bump `version`. Run the script.
#    It will fail and print the new SHA-256s for both files. Paste those
#    values into `sha256_js` and `sha256_css` in the TOML, then re-run.
python3 scripts/vendor_glightbox.py

# 3. Preview locally and open a page with an image. Click the image: the
#    lightbox should open and DevTools Network should show no unpkg.com
#    requests.
zensical serve

# 4. Commit scripts/vendor_glightbox.toml together with
#    docs/assets/javascripts/glightbox.min.js and
#    docs/assets/stylesheets/glightbox.min.css.
```

CI verifies the committed files match the pinned SHA-256 values for both
Mermaid and GLightbox. If you hand-edit a vendored file or forget to update
the SHA on upgrade, the build fails.

### Directory convention

Vendored third-party assets live under `docs/assets/` and are owned by the
scripts under `scripts/`:

- `docs/assets/javascripts/` holds vendored JS (`mermaid.tiny.js`,
  `glightbox.min.js`).
- `docs/assets/stylesheets/` holds vendored CSS alongside our own
  `extra.css`. The vendored `glightbox.min.css` lives here.

Treat all vendored files as read-only outputs of the vendoring scripts.
Do not hand-edit them. Upgrade by bumping the version pin in the
corresponding script's TOML and re-running the script. Vendored files are
marked `binary` in `.gitattributes` so PR diffs do not try to render
minified code.

First-party scripts we author and maintain live under `docs/javascripts/`,
for example `mermaid-init.js`, which initializes the vendored Mermaid
build. Both `docs/assets/javascripts/` and `docs/javascripts/` are served
from the same origin and register through `extra_javascript` in
`zensical.toml`.

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
