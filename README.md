# GitHub Copilot Framework - PHP to Java 21 Migration

## Document Summary

See the full [*Document Summary*](DocumentSummary.md).

## Change Log

See the full [*Change Log*](ChangeLog.md)

## Contents

- [GitHub Copilot Framework - PHP to Java 21 Migration](#github-copilot-framework---php-to-java-21-migration)
  - [Document Summary](#document-summary)
  - [Change Log](#change-log)
  - [Contents](#contents)
  - [1. Introduction](#1-introduction)
    - [Purpose of this Document](#purpose-of-this-document)
    - [Target Audience](#target-audience)
  - [Copilot Migration Agent Overview](#copilot-migration-agent-overview)
    - [Core Capabilities](#core-capabilities)
  - [Migration \& Modernization Process](#migration--modernization-process)
    - [Phase 0: Application Discovery](#phase-0-application-discovery)
    - [Phase 1: Technical Assessment](#phase-1-technical-assessment)
    - [Phase 2: Migration Planning](#phase-2-migration-planning)
    - [Phase 3: Code Migration](#phase-3-code-migration)
    - [Phase 4: Infrastructure Generation](#phase-4-infrastructure-generation)
    - [Phase 5: Deployment to Azure](#phase-5-deployment-to-azure)
    - [Phase 6: CI/CD Pipeline Setup](#phase-6-cicd-pipeline-setup)
  - [Key Features](#key-features)
  - [Tool Setup Guide](#tool-setup-guide)
    - [Requirements](#requirements)
    - [Azure Requirements](#azure-requirements)
    - [Installing Extensions](#installing-extensions)
    - [Sign in to use copilot](#sign-in-to-use-copilot)
  - [Repository Structure](#repository-structure)
  - [Skills Reference](#skills-reference)
    - [php-to-java-mapping](#php-to-java-mapping)
    - [jpa-hibernate-migration](#jpa-hibernate-migration)
    - [spring-boot-project-structure](#spring-boot-project-structure)
    - [azure-containerization](#azure-containerization)
  - [Guidelines for Migration with Sample Application](#guidelines-for-migration-with-sample-application)
    - [Phase 0: Application Discovery](#phase-0-application-discovery-1)
    - [Phase 1: Technical Assessment](#phase-1-technical-assessment-1)
    - [Phase 2: Migration Planning](#phase-2-migration-planning-1)
    - [Phase 3: Code Migration](#phase-3-code-migration-1)
    - [Phase 4: Infrastructure Generation](#phase-4-infrastructure-generation-1)
    - [Phase 5: Deploy To Azure](#phase-5-deploy-to-azure)
    - [Phase 6: CI/CD Pipeline Setup](#phase-6-cicd-pipeline-setup-1)
  - [Testing the Migrated Application](#testing-the-migrated-application)
    - [Avoiding Hallucinations](#avoiding-hallucinations)
  - [References](#references)

---

## 1. Introduction

This runbook provides comprehensive guidance for migrating PHP applications to Java 21 / Spring Boot 3.x for Azure deployment. It leverages GitHub Copilot's AI capabilities through custom agents, prompts, and skills to streamline and accelerate the migration process.

### Purpose of this Document

- Serve as a step-by-step guide for PHP to Java 21 / Spring Boot migration
- Document optimal practices for using GitHub Copilot agents in application migrations
- Provide troubleshooting and validation guidance for each migration phase
- Establish a framework for modernizing PHP applications to Java 21 on Azure

### Target Audience

- Development Teams migrating PHP applications
- Customers/Partners/Developers working with PHP and Java

---

## Copilot Migration Agent Overview

The PHP to Java 21 Migration Agent is an AI-assisted tool that leverages GitHub Copilot's capabilities specifically for migrating PHP applications to modern Java 21 / Spring Boot 3.x applications running on Azure. It combines conversational AI with technical expertise through:

- **Custom Agent**: `.github/agents/Code-Migration-Modernization.agent.md`
- **Phase Prompts**: `.github/prompts/` (7 migration phases)
- **Skills**: `.github/skills/` (mapping references and templates)

### Core Capabilities

1. **PHP Framework Detection**: Identifies Laravel, Symfony, CodeIgniter, Slim, vanilla PHP, and other frameworks
2. **Automated Code Transformation**: Converts PHP patterns to Java 21 / Spring Boot equivalents
3. **ORM Migration**: Transforms Eloquent/Doctrine to JPA / Hibernate
4. **Template Conversion**: Migrates Blade/Twig templates to Thymeleaf templates
5. **Infrastructure as Code Generation**: Creates Bicep/Terraform templates for Azure resources
6. **CI/CD Pipeline Creation**: Generates GitHub Actions and Azure DevOps pipelines
7. **Documentation Automation**: Creates and maintains migration status reports
8. **Interactive Problem-Solving**: Provides conversational guidance for issue resolution

---

## Migration & Modernization Process

The repository implements a structured 7-phase approach to PHP to Java 21 migration:

```mermaid
graph LR
    P0[Phase 0<br/>Discovery] --> P1[Phase 1<br/>Assessment]
    P1 --> P2[Phase 2<br/>Planning]
    P2 --> P3[Phase 3<br/>Migration]
    P3 --> P4[Phase 4<br/>Infrastructure]
    P4 --> P5[Phase 5<br/>Deployment]
    P5 --> P6[Phase 6<br/>CI/CD]
```

### Phase 0: Application Discovery

Analyze the PHP application to understand its components, business logic, and architecture. This phase creates the foundation for migration by documenting:

- PHP framework and version detection
- Controllers, models, services, and views inventory
- Business logic locations and flows
- Dependencies and integrations

**Prompt**: `/phase0-applicationdiscovery`

### Phase 1: Technical Assessment

Assess migration risks and gather user preferences for the Java / Spring Boot architecture:

- Evaluate migration complexity and risks
- Map PHP patterns to Java equivalents
- Gather preferences (Spring Boot MVC vs REST API, JPA/Hibernate vs JDBC, etc.)
- Estimate migration effort

**Prompt**: `/phase1-technicalassessment`

### Phase 2: Migration Planning

Create a detailed file-by-file migration plan:

- Document method-level mappings for services
- Define migration order by waves (dependencies first)
- Track business rules from source to target locations
- Generate `reports/Migration-Plan-Detailed.md`

**Prompt**: `/phase2-createmigrationplan`

### Phase 3: Code Migration

Execute the PHP to Java 21 migration following the plan:

- Create Java 21 / Spring Boot project structure
- Migrate controllers, services, and business logic
- Convert Eloquent models to JPA / Hibernate entities
- Transform Blade/Twig to Thymeleaf templates
- Validate with builds after each wave

**Prompt**: `/phase3-migratecode`

### Phase 4: Infrastructure Generation

Generate Azure infrastructure as code:

- Create Bicep or Terraform files
- Configure Azure hosting (App Service, Container Apps, AKS)
- Set up monitoring, security, and networking
- Include Application Insights and health checks

**Prompt**: `/phase4-generateinfra`

### Phase 5: Deployment to Azure

Deploy the Java 21 application to Azure:

- Use Azure Developer CLI (azd) for deployment
- Validate health and functionality
- Configure managed identities and Key Vault

**Prompt**: `/phase5-deploytoazure`

### Phase 6: CI/CD Pipeline Setup

Configure automated deployment pipelines:

- Set up GitHub Actions or Azure DevOps pipelines
- Include quality gates and security scanning
- Configure environment separation (dev/staging/prod)

**Prompt**: `/phase6-setupcicd`

---

## Key Features

- **PHP Framework Support**: Laravel, Symfony, CodeIgniter, Slim, Lumen, vanilla PHP
- **ORM Migration**: Eloquent/Doctrine to JPA / Hibernate with relationships
- **Template Conversion**: Blade/Twig syntax to Thymeleaf templates
- **Package Mapping**: Composer packages to Maven/Gradle equivalents
- **Authentication Migration**: Laravel Auth/Sanctum to Spring Security / Entra ID
- **Containerization**: Docker support with multi-stage builds for Azure deployment
- **Multiple Azure Targets**: App Service, Container Apps, AKS
- **Status Tracking**: Comprehensive migration progress reporting via `/getstatus`

---

## Tool Setup Guide

### Requirements

- GitHub Copilot License (Enterprise recommended)
- Visual Studio Code 1.101+ [Download](https://code.visualstudio.com/updates/v1_101)
- GitHub Copilot Extension 1.35+ [Download](https://marketplace.visualstudio.com/items?itemName=GitHub.copilot)
- GitHub Copilot Chat Extension 0.30+ [Download](https://marketplace.visualstudio.com/items?itemName=GitHub.copilot-chat)
- Azure MCP Server Extension [Download](https://marketplace.visualstudio.com/items?itemName=ms-azuretools.vscode-azure-mcp-server)
- GitHub Copilot for Azure Extension [Download](https://marketplace.visualstudio.com/items?itemName=ms-azuretools.vscode-azure-github-copilot)
- AZD CLI [Download](https://learn.microsoft.com/en-us/azure/developer/azure-developer-cli/install-azd)
- AZ CLI [Download](https://learn.microsoft.com/en-us/cli/azure/install-azure-cli-windows)
- JDK 21 (Eclipse Temurin recommended) [Download](https://adoptium.net/temurin/releases/?version=21)
- Docker Desktop (for containerization) [Download](https://www.docker.com/products/docker-desktop)

### Azure Requirements

- Azure subscription (Contributor / User Access Administrator role)
- Entra ID access for app registration (Application Administrator role)

### Installing Extensions

1. In Visual Studio Code, open the Extensions view from the Activity Bar
2. Search for and install the required extensions listed above
3. After installation, you should see confirmation notifications

### Sign in to use copilot

1. To use GitHub Copilot, sign in to your GitHub account in Visual Studio Code
2. The GitHub Copilot pane can be accessed by clicking the icon at the top of VS Code
3. For more information, refer to [Set up GitHub Copilot in VS Code](https://code.visualstudio.com/docs/copilot/setup)

---

## Repository Structure

```
.github/
├── agents/
│   └── Code-Migration-Modernization.agent.md    # Main migration agent
├── prompts/
│   ├── GetStatus.prompt.md                      # Check migration status
│   ├── Phase0-ApplicationDiscovery.prompt.md   # Discover PHP application
│   ├── Phase1-TechnicalAssessment.prompt.md    # Assess migration risks
│   ├── Phase2-CreateMigrationPlan.prompt.md    # Create file-by-file plan
│   ├── Phase3-MigrateCode.prompt.md            # Execute migration
│   ├── Phase4-GenerateInfra.prompt.md          # Generate Azure IaC
│   ├── Phase5-DeployToAzure.prompt.md          # Deploy to Azure
│   └── Phase6-SetupCICD.prompt.md              # Configure CI/CD
└── skills/
    ├── azure-containerization/                  # Docker & Azure container deployment
    ├── jpa-hibernate-migration/                 # Eloquent/Doctrine to JPA/Hibernate patterns
    ├── php-to-java-mapping/                     # PHP to Java mapping reference
    └── spring-boot-project-structure/           # Spring Boot project templates
Sample/
├── bookstore/                                   # Sample PHP application for migration
├── docker-compose.yml                          # Docker setup for running the PHP app
└── README.md                                   # Sample app documentation
```

---

## Skills Reference

The migration agent uses specialized skills that provide mapping references and templates:

### php-to-java-mapping

PHP to Java 21 code mapping reference including:

- Framework mapping (Laravel → Spring Boot MVC)
- Template syntax (Blade → Thymeleaf)
- Package mapping (Composer → Maven/Gradle)
- Authentication patterns

### jpa-hibernate-migration

JPA / Hibernate migration patterns:

- Eloquent models → JPA entities
- Relationship mapping (hasMany, belongsTo, belongsToMany)
- Soft deletes and timestamps
- Scopes → Spring Data JPA Specifications

### spring-boot-project-structure

Spring Boot 3.x project structure templates:

- Spring Boot MVC (from Laravel/Symfony)
- Spring Boot REST (from Slim/Lumen)
- Folder organization and naming conventions

### azure-containerization

Docker and Azure deployment best practices:

- Multi-stage Dockerfile templates
- docker-compose for local development
- Azure Container Apps configuration
- Health checks and security

---

## Guidelines for Migration with Sample Application

The repository includes a sample PHP Bookstore application (`Sample/bookstore/`) demonstrating the migration process.

**Sample Application Overview**:

- **Source**: Vanilla PHP with MySQL
- **Features**: User registration, login, profile editing, book management
- **Database**: MySQL with Docker support

### Phase 0: Application Discovery

**Prompt**: Enter the following in the chat window:

```text
/phase0-applicationdiscovery - Analyze the PHP application under Sample/bookstore
```

The agent will:

1. Detect PHP framework (vanilla PHP in this case)
2. Inventory all PHP files and their purposes
3. Map business logic and database operations
4. Generate `reports/Application-Discovery-Report.md`

### Phase 1: Technical Assessment

**Prompt**:

```text
/phase1-technicalassessment
```

Provide your preferences when asked:

- **Architecture**: Spring Boot MVC or REST API
- **Data Access**: JPA / Hibernate or Spring JDBC
- **Authentication**: Spring Security or Entra ID
- **Azure Hosting**: App Service, Container Apps, or AKS
- **IaC Tool**: Bicep or Terraform
- **Database**: Azure SQL, MySQL Flexible Server, or PostgreSQL

### Phase 2: Migration Planning

**Prompt**:

```text
/phase2-createmigrationplan
```

The agent will create a detailed file-by-file migration plan with:

- Source file → Target file mapping
- Method-level mappings for complex logic
- Migration order by waves
- Business rules tracking

### Phase 3: Code Migration

**Prompt**:

```text
/phase3-migratecode
```

The agent will:

1. Create Java 21 / Spring Boot project structure
2. Migrate controllers and services
3. Convert database access to JPA / Hibernate
4. Transform views to Thymeleaf
5. Build and validate after each wave

### Phase 4: Infrastructure Generation

**Prompt**:

```text
/phase4-generateinfra
```

Generates:

- Bicep or Terraform files in `infra/` folder
- Azure resource configurations
- Application Insights monitoring
- Security configurations

### Phase 5: Deploy To Azure

**Prompt**:

```text
/phase5-deploytoazure
```

Deploys using Azure Developer CLI (azd) with:

- Environment provisioning
- Application deployment
- Health validation

### Phase 6: CI/CD Pipeline Setup

**Prompt**:

```text
/phase6-setupcicd
```

Creates:

- GitHub Actions workflows in `.github/workflows/`
- Or Azure DevOps pipelines
- Quality gates and security scanning

---

## Testing the Migrated Application

1. After completing migration, build and run the Java 21 application locally
2. Test all functionality against the original PHP application behavior
3. Use the **@terminal** command to ask the agent for help with debugging
4. Run any existing tests and validate all business rules

### Avoiding Hallucinations

The guided prompts use status files in `reports/`:

- `reports/Report-Status.md` — migration status dashboard
- `reports/Application-Discovery-Report.md` — Phase 0 output
- `reports/Technical-Assessment-Report.md` — Phase 1 output
- `reports/Migration-Plan-Detailed.md` — Phase 2 output

> **Pro tip**: Use `/getstatus` at any time to check migration progress

> **Pro tip 2**: Use the **@terminal** command to debug issues

> **Pro tip 3**: Always verify business logic is preserved after migration

---

## References

- [Visual Studio Code](https://code.visualstudio.com)
- [GitHub Copilot](https://github.com/features/copilot)
- [GitHub Copilot Extension for VS Code](https://marketplace.visualstudio.com/items?itemName=GitHub.copilot)
- [Set up GitHub Copilot in VS Code](https://code.visualstudio.com/docs/copilot/setup)
- [Java 21 Documentation](https://docs.oracle.com/en/java/javase/21/)
- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Spring Data JPA](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)
- [Azure Developer CLI (azd)](https://learn.microsoft.com/azure/developer/azure-developer-cli)
- [Azure Container Apps](https://learn.microsoft.com/azure/container-apps)
- [Azure App Service](https://learn.microsoft.com/azure/app-service)
