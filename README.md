# CAS.AI Developer Docs
This repository stores documentation files for display on docs.page.

https://cleveradssolutions.docs.page/docs

For AI agents, the same documentation is published as
[llms.txt](https://cleveradssolutions.docs.page/docs/llms.txt) and
[llms-full.txt](https://cleveradssolutions.docs.page/docs/llms-full.txt).

## Agent skills
`plugins/` holds agent skills for integrating the SDK: the procedure, the questions to ask
a publisher first, and the rules an agent has to follow. See [plugins/README.md](plugins/README.md)
to use them, and [AGENTS.md](AGENTS.md) if your agent is not Claude.

They are kept here, next to the documentation, so that a page and the skill that quotes it
change in the same pull request, see [CONTRIBUTING.md](CONTRIBUTING.md). Nothing in
`plugins/` affects what docs.page publishes.
