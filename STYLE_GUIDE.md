# Style Guide

How to write documentation for AWS Lambda durable functions: grammar, voice,
and naming. For setup and the pull request workflow see
[CONTRIBUTING.md](CONTRIBUTING.md). For the technical authoring syntax (code
blocks, tabs, snippets, page structure) see the
[authoring guide](authoring-guide.md).

## Voice and Tone

Write for developers who want to learn how to use the SDK. Be direct and
technical. Avoid marketing language ("powerful", "flexible", "seamless").

Don't use unnecessary words. Keep sentences concise.

Use active voice. Write "the SDK checkpoints the result" not "the result is
checkpointed".

Every sentence must earn its place. If a sentence restates what the code
already shows, delete it.

Per Strunk and White, use definite, specific, concrete language. Prefer
the specific to the general, the definite to the vague, the concrete to
the abstract. In practice, avoid 'to be' and 'to have' where you can use
stronger verbs.

## Sentence Structure

Do not use emdash (—) or hyphen as a dash. If you feel tempted to use one,
break it into two sentences or use a comma.

Keep sentences short. One idea per sentence.

Do not use emdash or hyphen to separate a list item heading from its
description. Use bold or links instead:

```markdown
- **StepConfig** configures retry behavior and timeouts
- [wait()](operations/wait.md) pauses execution for a duration
```

## Naming

Use these product names consistently:

- **AWS Lambda durable functions** is the product's full name.
- **durable functions** is the casual short name for the product, always lowercase.
- **Durable Execution SDK** is the proper noun for the SDKs, capital D and E.
- **durable execution** describes an execution that happens to be durable, lowercase.

## Listicles

Do not create Terminology, Key Features, Best Practices, or FAQ sections. These
are convenient for LLMs but not for humans learning to code. If you feel
tempted to write one, fold the content into prose:

- New terms: introduce the term naturally in the prose where it first appears
- "Key features": drop them (usually marketing), or fold the underlying
    capability into the relevant section
- Best practices: fold the guidance into the section where it applies. A
    naming best practice belongs in the "Naming steps" section, not a generic
    "Best Practices" list.
- FAQ Q&As: each question is either (a) a concept that belongs in prose,
    (b) a code example that belongs in a code section, or (c) something
    already covered elsewhere that doesn't need repeating

## Revise, Don't Append

When you update a page, revise the whole section. A fresh reader should not
have to reconcile an opening sentence with a later correction tacked on at
the end.

If your new content qualifies, corrects, or extends something earlier on the
page, rewrite the earlier prose too. Don't leave stale sentences in place
because they were there first.

## Checklist

- [ ] No emdash, no hyphen-as-dash
- [ ] Active voice
- [ ] No marketing language
- [ ] Prose, not listicles (no Terminology, Key Features, Best Practices, FAQ)
- [ ] Product names used correctly
- [ ] Updates revise, they don't append
