# GitHub Copilot Migration & Modernization for Azure - ColdFusion to Java 21

This repository showcases how GitHub Copilot using custom agents, prompts, and skills can be leveraged to migrate ColdFusion (CFML) applications to Java 21 / Spring Boot 3.x for Azure deployment. The project provides a comprehensive 7-phase migration journey with enhanced tracking, status reporting, and a structured approach to the migration process.

## Overview

The GitHub Copilot ColdFusion to Java 21 Migration Framework provides a structured approach to:

1. **Discover** ColdFusion applications and understand their components, business logic, and architecture
2. **Assess** migration risks and gather user preferences for Java / Spring Boot architecture
3. **Plan** detailed file-by-file migration strategies
4. **Migrate** ColdFusion code to modern Java 21 with Spring Boot 3.x
5. **Generate** Azure infrastructure as code (Bicep/Terraform)
6. **Deploy** applications to Azure services
7. **Configure** CI/CD pipelines for automated deployment

Through a guided, AI-assisted workflow, developers can efficiently transform ColdFusion applications into modern, cloud-native Java 21 solutions running on Azure.

## Requirements

- GitHub Copilot License (Enterprise recommended)
- Model: Claude Sonnet 4.5 (Included in GitHub Copilot)
- Visual Studio Code 1.101+
- GitHub Copilot Extension 1.35+
- GitHub Copilot Chat Extension 0.30+
- Azure MCP Server Extension
- GitHub Copilot for Azure Extension
- AZD CLI (Azure Developer CLI)
- AZ CLI
- JDK 21 (Eclipse Temurin recommended)
- Docker Desktop (for containerization)

## Repository Structure

- **`.github/`**: Contains custom agents, prompts, and skills for GitHub Copilot
  - **`agents/`**: Migration agent definition (`Code-Migration-Modernization.agent.md`)
  - **`prompts/`**: Structured prompts for each phase of the migration process
  - **`skills/`**: Specialized skills with mapping references and templates
    - `coldfusion-to-java-mapping/` - ColdFusion to Java code conversion patterns
    - `jpa-hibernate-migration/` - CF-ORM/DataMgr/`<cfquery>` to JPA/Hibernate patterns
    - `spring-boot-project-structure/` - Spring Boot project templates
    - `azure-containerization/` - Docker and Azure deployment
    - `visual-baseline-capture/` - Screenshot capture (Phase 2) + visual spec (Phase 3)

- **`Sample/`**: Example ColdFusion (CFML) application for migration demonstration
  - A legacy `Application.cfm`-style project-management / issue-tracking app
  - Business components (CFCs) under `cfcs/` plus a `DataMgr` ORM
  - MySQL database

## Migration & Modernization Process

The repository implements a structured 7-phase approach to ColdFusion to Java 21 migration:

### Phase 0: Application Discovery (`/phase0-applicationdiscovery`)

Analyze the ColdFusion application to understand its components:
- Detect the CFML engine (Adobe ColdFusion, Lucee, Railo/BlueDragon) and framework (vanilla `Application.cfc`/`Application.cfm`, ColdBox, FW/1, Fusebox, Mach-II, Model-Glue, CFWheels)
- Inventory handlers/pages, components (CFCs), services, and views
- Map business logic locations and flows
- Document dependencies and integrations
- **Output**: `reports/Application-Discovery-Report.md`

### Phase 1: Technical Assessment (`/phase1-technicalassessment`)

Assess migration risks and gather preferences:
- Evaluate migration complexity and risks
- Map CFML patterns to Java equivalents
- Gather architecture preferences (Spring Boot MVC vs REST API, JPA/Hibernate vs JDBC)
- Estimate migration effort
- **Output**: `reports/Technical-Assessment-Report.md`

### Phase 2: Migration Planning (`/phase2-createmigrationplan`)

Create detailed file-by-file migration plan:
- Document method-level mappings for services
- Define migration order by waves (dependencies first)
- Track business rules from source to target
- Capture a visual baseline (screenshots) of each page for Phase 3
- **Output**: `reports/Migration-Plan-Detailed.md`, `reports/visual-baseline/`

### Phase 3: Code Migration (`/phase3-migratecode`)

Execute the ColdFusion to Java 21 migration:
- Create Java 21 / Spring Boot project structure
- Migrate handlers/pages, services, and business logic
- Convert CF-ORM / DataMgr / `<cfquery>` to JPA / Hibernate entities
- Transform `.cfm` / custom tags to Thymeleaf templates, matching the visual baseline
- Build and validate after each wave

### Phase 4: Infrastructure Generation (`/phase4-generateinfra`)

Generate Azure infrastructure as code:
- Create Bicep or Terraform files
- Configure App Service, Container Apps, or AKS
- Set up monitoring (Application Insights), security, and networking

### Phase 5: Deployment to Azure (`/phase5-deploytoazure`)

Deploy the Java 21 application:
- Use Azure Developer CLI (azd) for deployment
- Validate health and functionality
- Configure managed identities and Key Vault

### Phase 6: CI/CD Pipeline Setup (`/phase6-setupcicd`)

Configure automated deployment pipelines:
- Set up GitHub Actions or Azure DevOps pipelines
- Include quality gates and security scanning
- Configure environment separation

## Key Features

- **CFML Engine & Framework Detection**: Automatic detection of Adobe ColdFusion, Lucee, Railo/BlueDragon; vanilla `Application.cfc`/`Application.cfm`, ColdBox, FW/1, Fusebox, Mach-II, Model-Glue, CFWheels
- **ORM Migration**: CF-ORM / DataMgr / `<cfquery>` to JPA / Hibernate with relationships
- **Template Conversion**: `.cfm` / `<cfoutput>` / custom tags to Thymeleaf templates
- **Dependency Mapping**: CFML built-in tags / Java interop / modules to Maven/Gradle equivalents
- **Authentication Modernization**: `<cflogin>` / `<cfloginuser>` / `isUserInRole()` to Spring Security or Entra ID
- **Containerization**: Docker support with multi-stage builds
- **Multi-Platform Support**: App Service, Container Apps, AKS
- **Status Tracking**: Comprehensive progress reporting with `/getstatus`
- **Skills System**: Specialized skills with code examples and templates

## ColdFusion to Java 21 Mapping Quick Reference

| ColdFusion (CFML) | Java 21 |
|-------------------|---------|
| Vanilla CFML / ColdBox / FW/1 / Fusebox / Mach-II | Spring Boot MVC |
| Taffy / remote `.cfc` endpoints | Spring Boot REST |
| CF-ORM / DataMgr / `<cfquery>` | JPA / Hibernate |
| `.cfm` + `<cfoutput>` / custom tags | Thymeleaf templates |
| `<cflogin>` / `<cfloginuser>` | Spring Security |
| Built-in tags / `createObject("java")` / JavaLoader | Maven/Gradle dependencies |
| `settings.ini.cfm` / CF Admin / `Application.cfc this.*` | `application.yml` / `application.properties` |
| `<cfthread>` / `<cfschedule>` | Spring @Async / @Scheduled |

For complete mapping tables with examples, see `.github/skills/coldfusion-to-java-mapping/`.

## Avoiding Hallucinations

To reduce hallucinations during migration, the guided prompts use files in `reports/`:

- `reports/Report-Status.md` — overall migration status dashboard
- `reports/Application-Discovery-Report.md` — Phase 0 output
- `reports/Technical-Assessment-Report.md` — Phase 1 output
- `reports/Migration-Plan-Detailed.md` — Phase 2 output (file-by-file plan)
- `reports/visual-baseline/` — Phase 2 output (per-page screenshots + manifest.json)

During each phase, read the summary carefully to understand what will be delivered and what inputs are needed.

> **Pro tip**: Use `/getstatus` at any time to check migration progress
> **Pro tip 2**: Use the **@terminal** command to debug issues
> **Pro tip 3**: Always verify business logic is preserved after migration

## Getting Started

1. Clone this repository
2. Install [GitHub Copilot](https://copilot.github.com/) in Visual Studio Code
3. Open VS Code and select the ColdFusion to Java 21 Migration agent
4. Start with `/phase0-applicationdiscovery` to analyze your ColdFusion application
5. Use `/getstatus` at any time to check migration progress
6. Follow the guided prompts through all 7 phases

## Target Azure Hosting Platforms

The migration process supports multiple Azure hosting options:

- **Azure App Service**: For web applications and APIs
- **Azure Container Apps**: For containerized applications and microservices
- **Azure Kubernetes Service (AKS)**: For complex containerized applications

## Sample Application

The repository includes a ColdFusion (CFML) project-management / issue-tracking application under `Sample/` for demonstrating the migration:

- **Source**: Legacy CFML using `Application.cfm` (`<cfapplication>` with session-based login), mixing CFML tags and `<cfscript>`
- **Structure**: Root `.cfm` pages act as controllers + views; business logic lives in CFCs under `cfcs/`, instantiated in the `application` scope
- **Features**: Projects, milestones, issues/tickets, to-do lists, time tracking, billing & invoices, messages, file management, search, RSS, an SVN repository browser, plus `api/` and `mobile/` interfaces
- **Data Access**: `DataMgr` ORM (`cfcs/DataMgr/`) plus `<cfquery>`; Java interop via `JavaLoader.cfc` / `JavaProxy.cfc`
- **Database**: MySQL

> **Note**: The sample is the *source* application for the migration walkthrough. Use `/phase0-applicationdiscovery` to begin analyzing it.

## Contributing

Contributions to improve the agents, prompts, skills, or add new features are welcome. Please feel free to submit pull requests or open issues to discuss potential improvements.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
