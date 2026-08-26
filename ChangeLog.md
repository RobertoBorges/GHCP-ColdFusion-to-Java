# Change Log

This section documents the change history of the document. Revisions must be tracked by identifying a latest version number, the date of modification, the person responsible for the change, and the reason for the change.

## [3.2.0] - 2026-08-26

### Added
- Per-phase **model tiering**: every prompt now declares a `model:` matched to its workload, plus a new **Model Selection & Cost** guide (tier → phase → model table with override instructions) in `README.md` and `GitHubCopilot-Migration-Framework.md`

### Changed
- Phase 2 (Planning) and Phase 3 (Code Migration) now use `Claude Opus 4.6 (copilot)` — the quality-critical phases that drive and generate the migrated Java code
- Phase 4 (Infra), Phase 5 (Deploy), Phase 6 (CI/CD), and `/getstatus` now use `GPT-5 mini (copilot)` — templated / mechanical work
- Phase 0 (Discovery), Phase 1 (Assessment), and the orchestrator agent's free-chat default remain on `Claude Sonnet 4.6 (copilot)`

## [3.1.0] - 2026-08-26

### Added
- New **visual-baseline-capture** skill: hybrid capture (automated Playwright crawl + guided manual) of a screenshot per page/state of the running legacy app, cataloged in a `manifest.json` that maps each `.cfm` page → screenshot → target Thymeleaf view. Ships a `capture-screenshots.mjs` Playwright template plus `routes`/`manifest` examples
- Phase 2 gained a **"Capture the Visual Baseline"** step that produces `reports/visual-baseline/` (screenshots + manifest), a coverage gate, a `Visual Reference` field on each view plan, and a Visual Baseline section in the plan document
- Phase 3 now opens the matching screenshot as the **layout spec** when migrating each view, with an optional visual-fidelity (regression) check that re-screenshots the migrated page and diffs it against the baseline
- Local Docker environment under `Sample/Docker/` to run the sample ColdFusion app (Lucee 5 + MySQL 5.7) so its baseline can be captured

### Changed
- Agent guardrails: capturing a visual baseline is now a Phase 2 deliverable, and consulting it is a Phase 3 rule; skills list, reports list, and phase summaries updated
- Phase 0 discovery notes that the page/view inventory and user journeys seed the Phase 2 capture list

## [3.0.0] - 2026-08-25

### Changed
- Converted the migration framework's **source** side from PHP to ColdFusion (CFML); the Java 21 / Spring Boot 3.x / Azure target is unchanged
- Rewrote the agent, all phase prompts, and skills to detect and map CFML instead of PHP (Adobe ColdFusion, Lucee, Railo/BlueDragon; vanilla `Application.cfc`/`Application.cfm`, ColdBox, FW/1, Fusebox, Mach-II, Model-Glue, CFWheels)
- Replaced Eloquent/Doctrine guidance with CF-ORM / DataMgr / `<cfquery>` → JPA / Hibernate patterns
- Replaced Blade/Twig guidance with `.cfm` / `<cfoutput>` / custom tags → Thymeleaf
- Replaced Laravel Auth guidance with `<cflogin>` / `<cfloginuser>` / `isUserInRole()` → Spring Security
- Replaced Composer package mapping with CFML built-in tags / Java interop / modules → Maven/Gradle
- Emphasized MySQL as the primary source database and switched local-dev docker-compose to MySQL 8
- Renamed the agent display name to "ColdFusion to Java Migration"
- Replaced the sample application section with the real ColdFusion (CFML) project-management / issue-tracking app under `Sample/`

### Added
- New skill: coldfusion-to-java-mapping (replaces php-to-java-mapping)

### Removed
- Removed the PHP-oriented skill: php-to-java-mapping (renamed to coldfusion-to-java-mapping)

## [2.0.0] - 2026-03-31

### Changed
- Converted entire migration framework from PHP-to-.NET to PHP-to-Java
- Replaced all .NET 10 / ASP.NET Core references with Java 21 / Spring Boot 3.x
- Replaced EF Core skills with JPA/Hibernate migration patterns
- Replaced Razor view guidance with Thymeleaf templates
- Updated Azure deployment for Java runtime (App Service Java, Eclipse Temurin containers)
- Updated CI/CD for Maven/Gradle builds, JaCoCo coverage, SpotBugs analysis

### Added
- New skill: php-to-java-mapping (replaces php-to-dotnet-mapping)
- New skill: jpa-hibernate-migration (replaces ef-core-migration)
- New skill: spring-boot-project-structure (replaces dotnet-project-structure)

### Removed
- Removed .NET-specific skills: php-to-dotnet-mapping, ef-core-migration, dotnet-project-structure
- Removed irrelevant skills: crystal-reports-migration, winforms-to-web

| Date       | Version | Change Description | Author                | Reviewer           | Approver |
|------------|---------|--------------------|------------------------|--------------------|----------|
| 08-25-2026 | v3.0    | Converted framework source side from PHP to ColdFusion (CFML); Java 21 / Spring Boot target unchanged | GitHub Copilot | | |
| 03-31-2026 | v2.0    | Converted framework from PHP-to-.NET to PHP-to-Java | GitHub Copilot | | |
| 03-02-2026 | v1.1    | Updated for PHP to .NET 10 migration framework | GitHub Copilot | | |
| 30-05-2025 | v1.0    | Initial draft      | PraveenChand Kopila   | Jayanta Chatterjee | Yulin Shi |

[*back to content*](README.md)
