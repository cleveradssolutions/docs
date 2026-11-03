# For agents that are not Claude

This repository holds the CAS.AI documentation, published at
`https://docs.page/cleveradssolutions/docs`, and, under `plugins/`, agent skills for
integrating the SDK.

The skills are plain Markdown. There is no runtime, no tool call and no Claude specific
syntax inside them; the only Claude specific files are `.claude-plugin/marketplace.json`
and `plugins/cas-sdk/.claude-plugin/plugin.json`, which other agents can ignore.

## Entry points

| You want | Read |
|---|---|
| Integrate CAS.AI into an Android app | `plugins/cas-sdk/skills/cas-android/SKILL.md` |
| Integrate CAS.AI into an iOS app | `plugins/cas-sdk/skills/cas-ios/SKILL.md` |
| The rules that apply to any platform | `plugins/shared/agent-rules.md` |
| How to ask the publisher anything | `plugins/shared/question-format.md` |
| What to ask, per platform | the skill's `references/questions.md` |
| Where the canonical documentation is | `plugins/shared/docs-map.md` |
| Unity, CAS Publishing, Firebase | `llm-guides/` |

Installed rather than read: the [`skills`](https://github.com/vercel-labs/skills) CLI
copies these into the skills directory of whichever agents you have.

```bash
npx skills add cleveradssolutions/docs/plugins/cas-sdk/skills
```

`SKILL.md` links to files inside its own `references/` folder, relative to itself. Follow
them as you would any relative link.

## How to use it

1. Read the `SKILL.md` for the platform in full. It is short on purpose.
2. Read `references/agent-rules.md` before writing code. Those rules are not style
   preferences; breaking the first one can get the publisher's ad account flagged.
3. Read a reference file when the phase that links to it begins, not before. They exist so
   the main procedure can stay small.
4. Fetch the documentation page a reference names when you need the full detail or the
   current version numbers. The whole documentation is also published for agents at
   `https://docs.page/cleveradssolutions/docs/llms.txt`, with the full text at
   `llms-full.txt`, which is large, so prefer single pages.

## If you are copying this into a project

Copy one skill folder, keep `references/` next to its `SKILL.md`, and keep the link to the
documentation. Do not paste the reference files into a system prompt and drop the
documentation links: the version numbers and the network lists in this repository go
stale, the documentation does not.
