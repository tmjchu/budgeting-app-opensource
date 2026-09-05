# Logo verification

Run `npm run test:logos` with Node 22.6+ (uses Node's TypeScript stripping).
These focused checks cover deterministic identity matching, collisions, and
account-type fallbacks. `npm run build` checks the production TypeScript graph.

For visual QA, run `npm run mock -- --host 127.0.0.1 --port 5175` and open
`http://127.0.0.1:5175/tests/logo-qa.html`. This development-only entrypoint is
not included in the production bundle. Its CSP blocks external requests.
Check every bundled logo, unknown credit/deposit accounts, empty metadata,
long names, and the Break/Restore Chase buttons. Also inspect the real mock
dashboard and Accounts page at desktop and narrow widths.

The break button changes only the catalog loaded in that fixture's browser
page, never source assets or persisted account data. Reloading resets it.
