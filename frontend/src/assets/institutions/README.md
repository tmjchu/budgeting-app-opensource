# Bundled institution logos

Source: [Bank Logos Collection](https://github.com/auraveni/global-bank-logos),
revision `ad33060ca976397a9fcb46dd40c2d77bce5ce7e1`, downloaded 2026-09-05.

| Local file | Institution | Pinned source |
| --- | --- | --- |
| chase.svg | Chase | [SVG](https://github.com/auraveni/global-bank-logos/blob/ad33060ca976397a9fcb46dd40c2d77bce5ce7e1/assets/bank/international-bank/chase.svg) |
| boa.svg | Bank of America | [SVG](https://github.com/auraveni/global-bank-logos/blob/ad33060ca976397a9fcb46dd40c2d77bce5ce7e1/assets/bank/international-bank/boa.svg) |
| wells.svg | Wells Fargo | [SVG](https://github.com/auraveni/global-bank-logos/blob/ad33060ca976397a9fcb46dd40c2d77bce5ce7e1/assets/bank/international-bank/wells.svg) |
| citi.svg | Citi | [SVG](https://github.com/auraveni/global-bank-logos/blob/ad33060ca976397a9fcb46dd40c2d77bce5ce7e1/assets/bank/international-bank/citi.svg) |
| american-express.svg | American Express | [SVG](https://github.com/auraveni/global-bank-logos/blob/ad33060ca976397a9fcb46dd40c2d77bce5ce7e1/assets/bank/international-bank/american-express.svg) |

The upstream repository distributes these assets under MIT. Its complete license
is retained in LICENSE; retain it when redistributing these assets. That license
does not grant bank trademark rights or prove separate permission from each bank.
These marks are used to identify institutions, with no implied affiliation or
endorsement. No bank-specific authorization is asserted.

Files are unchanged from the pinned sources. Reviewed SVG markup contains no
scripts, foreignObject elements, event handlers, or external asset references;
local clipping paths and local style rules are retained. Display through `<img>`
with preserved colors and aspect ratio. No runtime download or logo API is used.

To add a bank, review its asset and source terms, record the source here, import
it in `src/lib/institutions.ts`, and add exact institution-name aliases. Never
match account nicknames. Provider ID arrays are intentionally empty until IDs
are verified against a source (do not guess production IDs from sandbox data).
Capital One, Discover, U.S. Bank, PNC, and Ally currently use generic icons.
