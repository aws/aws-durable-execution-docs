# Style Guide

This guide covers how to write documentation for AWS Lambda Durable Functions:
grammar, voice, and sentence construction. It sits alongside two other
documents. [CONTRIBUTING.md](CONTRIBUTING.md) covers the setup and pull request
workflow. The [authoring guide](authoring-guide.md) covers the technical syntax
of a page: code blocks, content tabs, snippet references, and page structure.
This guide covers the words themselves.

Read this before you write. Run through the [checklist](#checklist) before you
open a pull request.

## Voice

Write for a developer who wants to learn the SDK. Be direct and technical. The
reader came for working code and a clear explanation, so give them that and
nothing else.

Use active voice. Name the thing that acts, then say what it does. Write "the
SDK checkpoints the result", not "the result is checkpointed". Active voice
tells the reader who does what, which matters when the actor is the SDK, the
runtime, or their own handler. Passive voice hides the actor and forces the
reader to guess.

Prefer strong, specific verbs. Reach past "to be" and "to have" when a verb
carries the meaning. Write "the step retries on failure", not "the step has
retry behavior on failure". Concrete verbs shorten the sentence and sharpen it
at the same time.

Every sentence must earn its place. If a sentence restates what the code
already shows, delete it. If a sentence repeats the sentence before it, cut one
of them.

## Sentences

Keep sentences short. Put one idea in each. When a sentence grows a second
clause that qualifies the first, split it in two.

Do not use an em-dash (`—`). Do not use a hyphen as a dash either. When you
feel the pull toward a dash, you have three better options. Use a comma for a
light pause. Use a sub-clause set off by commas for an aside. Start a new
sentence for a full stop. For example, write "The step runs once, then the SDK
stores its result" rather than joining the two clauses with a dash.

The same rule applies inside a list item. Do not use a dash to separate a list
item's label from its description. Use bold or a link instead:

```markdown
- **StepConfig** configures retry behavior and timeouts
- [wait()](docs/sdk-reference/operations/wait.md) pauses execution for a duration
```

## Prose over lists

Write conceptual content as prose. Short paragraphs, not bullet points.

A list is right for a set of parallel, concrete items: parameter names, file
paths, ordered steps a reader follows in sequence. A list is wrong for an idea.
When you turn an explanation into bullets, you drop the connective tissue that
tells the reader how one point leads to the next, and you leave them to
reassemble the argument on their own.

This means no Terminology, Key Features, Best Practices, or FAQ sections. Those
shapes read well to a machine and poorly to a person learning to code. Fold
their content into the prose where it belongs. Put a naming guideline in the
section about naming, not in a generic "Best Practices" list. Turn each FAQ
question into either a sentence in the relevant section or a code example, and
drop it if another page already answers it.

## Terminology

Introduce a term where it first appears, in the prose that needs it. Do not
gather definitions into a Terminology or Glossary section. A reader meets a
word in context and learns it in context, so define "checkpoint" the first time
you describe the SDK saving a result, not in a list they have to cross-check.

Name a thing once, then call it that name every time. When the same concept
picks up two labels, the reader cannot tell whether you mean one thing or two.
If a step's saved output is "the result", do not later call it "the return
value" or "the output"; pick one word and hold to it across the page.

Match the SDK's own vocabulary. Use the names the source code uses for methods,
types, and parameters, so the prose and the code examples reinforce each other
instead of teaching two dialects.

## No marketing language

Describe what the SDK does, not how it makes the reader feel. Cut words like
"powerful", "flexible", "seamless", "robust", "simply", and "just". They add no
information, and a developer reading reference documentation did not come for a
sales pitch.

Show a capability through a concrete example and a plain description of its
behavior. If a feature is genuinely useful, the working code demonstrates that
better than an adjective can.

## Revise, do not append

When you update a page, revise the whole section rather than tack a correction
onto the end. A fresh reader should not have to reconcile an opening sentence
with a note added later that contradicts it. If your new content qualifies or
corrects an earlier sentence, rewrite that earlier sentence too. Leave the
section as one coherent explanation, not a paper trail of edits.

## Language neutrality

Keep shared prose neutral across TypeScript, Python, Java, and C#. No language
is the default. When a point applies to only one language, the
[authoring guide](authoring-guide.md) shows how to place it inside that
language's tab. This guide's rule is simpler: do not describe a quirk of one
language as though it were true of all four.

## Checklist

Before you open a pull request, confirm the writing holds to this guide:

- [ ] No em-dash and no hyphen used as a dash
- [ ] Active voice, with the actor named
- [ ] Conceptual content reads as prose, not bullet points
- [ ] No Terminology, Key Features, Best Practices, or FAQ sections
- [ ] Each term introduced in prose where it first appears, named consistently
- [ ] No marketing adjectives ("powerful", "flexible", "seamless", "simply")
- [ ] Short sentences, one idea each
- [ ] Updates revise the section rather than append to it
- [ ] Shared prose stays neutral across all four languages

For the technical side of authoring (code samples, tabs, page structure, and
the full pre-commit checklist), see the [authoring guide](authoring-guide.md).
