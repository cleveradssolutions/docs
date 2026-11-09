# The question format

The same in every CAS guide for agents, here and in `llm-guides/`. The questions
themselves are per platform; this is only the shape they are asked in.

**Research first, ask second.** Everything the project answers is read, not asked.

- One question = one short bold line, then `Found:` (the evidence and where it came from,
  or "nothing found"), then a table of 2-4 ready options with a letter (two columns only:
  the letter and the answer). Right after the table, one line:
  `**Recommended:** Option A - the reason in a few words`. Add a last option "Other" only
  when a free value is possible. When there is nothing to recommend, a value only the
  publisher has, write `**Recommended:** none - <why>`.
- Do not ask what the research settled. List it under `Found:` so the publisher can
  object.
- **Rounds of at most 3 questions**, numbered through all rounds (Q1-Q3, then Q4-Q6), each
  round titled "Round 1 of 3". Send one round, wait, then the next. Order them so a
  question an earlier answer decides comes after it, and skip one an earlier answer made
  pointless.
- Separate the parts of a question with empty lines, and the questions from each other
  with a `---` line.
- End each round with: "Reply `ok` to accept every Recommended answer of this round, or
  list the changes, for example `2B`."
- `ok` accepts every Recommended answer of the round. A question with
  `Recommended: none` stays open: ask only that one again, in a short follow-up, before
  the next round. Never re-ask what `ok` settled.
- After the reply, say in one line what was chosen and start at once.
- A question that comes up later, from an error or a result, uses the same format, one
  question per message.

Example:

> **Q1. Which CAS ID?**
> Found: `applicationId` is `com.yourcompany.yourgame` (`app/build.gradle.kts`).
>
> | Option | Answer |
> |---|---|
> | A | `com.yourcompany.yourgame` |
> | B | Other |
>
> **Recommended:** Option A - the application id of the module being integrated.
