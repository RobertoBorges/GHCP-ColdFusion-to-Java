# Plan: Add a "Visual Baseline" capture step to the migration framework

## Context

`GHCP-ColdFusion-to-Java` is a GitHub Copilot custom-agent framework that guides a 7-phase
**ColdFusion (CFML) → Java 21 / Spring Boot 3.x** migration (agent + phase prompts + skills + docs).
A real legacy CFML app (Project Tracker v2.6.3) lives under `Sample/ProjectTrackerSrc/`, with a local
Docker run environment under `Sample/Docker/`.

### Previously completed (not this work stream)
- [x] Converted the framework from PHP→Java guidance to ColdFusion→Java guidance (committed).
- [x] Removed the unused root `images/` folder (committed).
- [x] Built + verified a local Docker environment (Lucee 5 + MySQL 5.7) that runs the sample app.
- [x] Reviewed the user's VS Code reorg (`Sample/ProjectTrackerSrc/`, `Sample/Docker/`); fixed a stale
      running stack, a broken README screenshot reference, and a compose comment.

## Problem & Goal (this work stream)

Testing the framework end-to-end showed the migrated UI drifts badly from the original — the model has
no picture of what each screen *looked like*. **Goal:** add a **visual baseline** capture step so that
**Phase 2 (Create Migration Plan)** interactively walks the user through the running legacy app and
captures a screenshot (plus a short behavior note) of **each page/state**, cataloged in a manifest that
maps `legacy .cfm page → screenshot → target Java view`. **Phase 3 (Migrate Code)** then opens each
screenshot as a **multimodal visual spec** when generating the Thymeleaf/CSS, so the migrated screen
matches the original.

## Confirmed decisions (from user)

- **Capture strategy:** Hybrid — automated crawl + guided manual fallback.
- **Automation tool:** Playwright (login/session reuse, full-page, multi-viewport).
- **Manifest:** `manifest.json` + a rendered Markdown table in the Phase 2 report.
- **Reusable skill:** Yes — a `visual-baseline-capture` skill holding the capture-script template.
- **Scope now:** Update `plan.md`, then implement Phase 2 + Phase 3 changes (+ skill).

## Design

### Artifacts
- Screenshots + manifest live under `reports/visual-baseline/` (version-controlled, outside app source).
- `reports/visual-baseline/manifest.json` — one entry per screenshot:
  `id, sourcePage (.cfm), url, authState, params, viewport, screenshot, states[], notes,
  behaviorNotes, targetView (filled in Phase 3)`.
- A rendered Markdown table of the manifest is embedded in `reports/Migration-Plan-Detailed.md`.

### Capture strategy (3 tiers, orchestrated interactively in Phase 2)
1. **Automated crawl (Tier 1):** Playwright script seeded with base URL + credentials + the Phase 0
   route list; logs in once, reuses session, full-page screenshots at a desktop viewport (+ mobile
   viewport for the `mobile/` interface).
2. **Guided manual (Tier 2):** for pages the crawler can't reach (record-ID params, wizard steps,
   modals, validation-error states, PDF/email output, role-specific views) the agent gives a numbered
   checklist + naming convention + target folder; the user drops images in.
3. **Interactive walkthrough (Tier 3):** the agent reconciles auto vs manual, tracks **coverage**
   against the Phase 0 inventory, and loops until complete.

### Best practices baked in
- Capture states, not just pages (empty/populated, validation errors, role variants, mobile width).
- Full-page screenshots + fixed viewport; pair each with a one-line behavior note.
- Name by route/`.cfm` file for 1:1 mapping + measurable coverage.
- Use non-prod data (the seeded Docker DB); keep script + routes committed so captures are re-runnable.
- **Optional Phase 3 visual regression:** re-screenshot the migrated page and diff vs the baseline.

## Work breakdown

1. **Skill** — `.github/skills/visual-baseline-capture/`: `SKILL.md` + `templates/capture-screenshots.mjs`
   (Playwright) + `templates/routes.example.json` + `templates/manifest.example.json`.
2. **Phase 2 prompt** — new "Capture Visual Baseline" step (3-tier flow + manifest schema + coverage
   gate); add a `Visual Reference` field to the §3.4 Views template; add a Visual Baseline section to
   the plan document, status report, and deliverables.
3. **Phase 3 prompt** — Step 1 reads the manifest; Wave 5 §5.2 opens the matching screenshot as the
   visual spec before generating each template; new §5.4 optional visual-fidelity check.
4. **Phase 0 prompt** — light note that the page/route inventory + user journeys seed the capture list.
5. **Agent** — capability note, a Phase 2 `@agent rule` (baseline is a deliverable), a Phase 3
   `@agent rule` (consult the baseline), reports list + skills list + quick-ref updates.
6. **Docs** — README phase text + a ChangeLog entry.
7. **Verify** — cross-reference the manifest path across skill/Phase2/Phase3; confirm links intact.

## Out of scope
- Migrating the Sample app itself.
- Changing the Java 21 / Spring Boot 3.x target or Azure content.

## Notes / risks
- Automated capture requires the legacy app to be running and reachable (the Docker env covers the
  sample); manual capture is the always-available fallback when it can't run or terminal execution is
  unavailable (e.g., Copilot Chat without tools).
- Screenshots may contain data — steer users to a non-production dataset.
