---
name: visual-baseline-capture
description: Capture a visual baseline (screenshots) of a running legacy ColdFusion app during Phase 2 and consume it during Phase 3. Use when the migration plan needs a per-page visual reference. Covers the hybrid capture strategy (automated Playwright crawl with login + full-page + mobile viewport, plus guided manual capture) and the manifest.json that maps each legacy .cfm page to its screenshot and target Thymeleaf view.
---

# Visual Baseline Capture

> Use this skill in **Phase 2** to capture what the legacy ColdFusion app *looked like* before migration,
> and in **Phase 3** to reproduce each screen faithfully. The screenshots are a **multimodal visual spec**:
> a Copilot model can open each image and generate Thymeleaf/CSS that matches the original layout.

## What a visual baseline is (and why)

A **visual baseline** is one screenshot (plus a short behavior note) per page/state of the running legacy
app, cataloged in a manifest that maps `legacy .cfm page → screenshot → target Java view`. Without it, the
migrated UI drifts from the original because the model is guessing at layout, spacing, tables, and branding.
With it, Phase 3 has a concrete acceptance target per screen.

## Where it lives

```
reports/visual-baseline/
├── manifest.json          # machine-readable catalog (one entry per screenshot)
├── routes.json            # the auto-capture route list (seeded from Phase 0 inventory)
├── home.png               # screenshots, named by route / .cfm file
├── allissues.png
├── account.png
├── login.png
└── ...
```

The manifest is also rendered as a **Markdown table** inside `reports/Migration-Plan-Detailed.md` so it is
human-reviewable alongside the plan.

## Manifest schema

One object per screenshot in `manifest.json` (a JSON array):

```jsonc
{
  "id": "home",                         // stable id; matches the screenshot filename
  "sourcePage": "index.cfm",            // legacy CFML file / route this screen renders from
  "url": "/index.cfm",                  // path used to reach it (relative to base URL)
  "authState": "authenticated:user",    // public | authenticated:user | authenticated:admin | ...
  "params": null,                        // required query/params, e.g. "?p=1" (project id), or null
  "viewport": "1280-full",              // "<width>-full" desktop, or "mobile" for the mobile UI
  "screenshot": "home.png",             // path relative to reports/visual-baseline/
  "states": ["default"],                // variants captured: default | empty | validation-error | ...
  "notes": "Top nav, project table, sidebar filters",   // key UI elements
  "behaviorNotes": "AJAX table refresh on filter change", // dynamic behavior a static image can't show
  "targetView": ""                       // filled in Phase 3, e.g. "templates/home.html"
}
```

## Capture strategy — hybrid (recommended)

Run this interactively with the user. Reconcile the two tiers and track **coverage** against the Phase 0
page/route inventory until every page is either captured or explicitly marked N/A (with a reason).

### Tier 1 — Automated crawl (when the app runs)

Uses `templates/capture-screenshots.mjs` (Playwright). It logs in once, reuses the session, and takes
**full-page** screenshots at a desktop viewport (and a mobile viewport for routes flagged `"viewport": "mobile"`).

**Prerequisites**
- The legacy app is running and reachable (for the sample: `cd Sample/Docker && docker compose up -d`,
  then http://localhost:8080). Use a **non-production** dataset — screenshots may contain data.
- Node.js 18+ and Playwright's Chromium:
  ```bash
  npm init -y            # if there is no package.json in the working dir
  npm i -D playwright
  npx playwright install chromium
  ```

**Configure & run** (copy the templates into `reports/visual-baseline/` first, then edit `routes.json`):
```bash
# From the repo root. Credentials via env vars so they are never committed.
$env:BASE_URL   = "http://localhost:8080"   # PowerShell;  export BASE_URL=... on bash
$env:VB_USERNAME= "admin"
$env:VB_PASSWORD= "admin"
node reports/visual-baseline/capture-screenshots.mjs reports/visual-baseline/routes.json
```
The script writes the `.png` files and (re)writes `manifest.json` in the output folder, recording a
`captured` status per route so failures are visible.

**Login is configurable** via env vars (defaults match the Project Tracker sample, which posts
`username`/`password` handled by `<cflogin>` in `Application.cfm`):
`VB_LOGIN_PATH` (default `/index.cfm`), `VB_USER_SELECTOR` (`input[name="username"]`),
`VB_PASS_SELECTOR` (`input[name="password"]`), `VB_SUBMIT_SELECTOR`
(`input[type=submit], button[type=submit], button:has-text("Login")`), and `VB_LOGGED_IN_TEXT`
(text expected only when logged in, used to verify login succeeded).

**Public vs. authenticated pages** are captured in separate browser contexts: routes with
`"authState": "public"` are captured **without a session**, while `authenticated:*` routes are captured
in a logged-in context. This is why a public login page and the post-login home render correctly even
when they share a URL (e.g. both `/index.cfm`). Mobile variants log in only when the route isn't public.

### Tier 2 — Guided manual capture (always-available fallback)

Use for pages the crawler can't reach or shouldn't drive automatically:
- pages needing a specific record id/param (e.g. `people.cfm?p=1`, `editIssue.cfm?i=42`),
- multi-step wizards, modals/pop-ups, drag-and-drop UIs,
- **validation-error** and **empty** states,
- role-specific variants (admin vs guest),
- non-HTML output (generated PDFs, emails, exports).

Give the user a **numbered checklist** derived from the Phase 0 inventory, this naming convention, and the
target folder `reports/visual-baseline/`:
- **Naming:** `<id>.png` where `<id>` mirrors the route/`.cfm` file (e.g. `admin-settings.png`,
  `editissue-validation-error.png`). Use one file per state.
- After the user drops files in, **add a manifest entry** for each (set `"states"` and `"notes"`,
  `"behaviorNotes"` from what the user tells you).

### Tier 3 — Interactive walkthrough & coverage gate

1. Load the Phase 0 page/route inventory + user journeys → build the candidate page list.
2. Split into **auto-capturable** (public + list pages, no special params) vs **needs-context** (Tier 2).
3. Confirm base URL + credentials, run Tier 1, then walk the user through Tier 2 items.
4. Compute **coverage**: `captured pages / total inventory pages`. List every not-captured page with a
   reason (N/A, deferred, blocked). Do not finish Phase 2 until coverage is acknowledged by the user.

## Best practices

- **Capture states, not just pages** — empty vs populated, validation errors, role variants, and the
  **mobile** interface at a mobile viewport.
- **Full-page screenshots** at a fixed viewport width for layout fidelity and consistency.
- **One behavior note per screen** — a static image can't convey hover/AJAX/JS; a one-liner fills the gap.
- **Name by route/`.cfm` file** so the mapping to the migration plan is 1:1 and coverage is measurable.
- **Non-prod data** — use the seeded Docker DB or a scratch dataset; never capture real customer data.
- **Keep `routes.json` + the script committed** so captures are re-runnable after data or UI changes.

## How Phase 3 consumes the baseline

- Before generating each Thymeleaf template (Wave 5), **open the matching screenshot** (via the manifest
  `screenshot` path) and treat it as the visual acceptance target — match layout, spacing, labels, table
  columns, nav structure, and branding/CSS. Record the produced template in the manifest `targetView`.
- **Optional visual-fidelity check:** after migrating a view, run the app and re-screenshot the same route,
  then compare side-by-side (or pixel-diff) against the baseline to catch regressions objectively.

## Fallbacks

- **App can't be run:** skip Tier 1 and rely entirely on Tier 2 (any existing screenshots, design docs, or
  the user manually capturing from a staging/production URL).
- **No terminal execution (e.g. Copilot Chat without tools):** the agent produces the checklist + manifest
  and the user runs the Playwright script (or captures manually) and drops files into the folder.
