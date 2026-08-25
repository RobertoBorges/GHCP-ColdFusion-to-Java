# GitHub Copilot Migration & Modernization for Azure - PHP to Java 21

This repository showcases how GitHub Copilot using custom agents, prompts, and skills can be leveraged to migrate PHP applications to Java 21 / Spring Boot 3.x for Azure deployment. The project provides a comprehensive 7-phase migration journey with enhanced tracking, status reporting, and a structured approach to the migration process.

## Overview

The GitHub Copilot PHP to Java 21 Migration Framework provides a structured approach to:

1. **Discover** PHP applications and understand their components, business logic, and architecture
2. **Assess** migration risks and gather user preferences for Java / Spring Boot architecture
3. **Plan** detailed file-by-file migration strategies
4. **Migrate** PHP code to modern Java 21 with Spring Boot 3.x
5. **Generate** Azure infrastructure as code (Bicep/Terraform)
6. **Deploy** applications to Azure services
7. **Configure** CI/CD pipelines for automated deployment

Through a guided, AI-assisted workflow, developers can efficiently transform PHP applications into modern, cloud-native Java 21 solutions running on Azure.

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
    - `php-to-java-mapping/` - PHP to Java code conversion patterns
    - `jpa-hibernate-migration/` - Eloquent/Doctrine to JPA/Hibernate patterns
    - `spring-boot-project-structure/` - Spring Boot project templates
    - `azure-containerization/` - Docker and Azure deployment

- **`Sample/`**: Example PHP Bookstore application for migration demonstration
  - **`bookstore/`**: Vanilla PHP application with MySQL
  - **`docker-compose.yml`**: Docker setup for running the PHP app

## Migration & Modernization Process

The repository implements a structured 7-phase approach to PHP to Java 21 migration:

### Phase 0: Application Discovery (`/phase0-applicationdiscovery`)

Analyze the PHP application to understand its components:
- Detect PHP framework (Laravel, Symfony, CodeIgniter, Slim, vanilla PHP)
- Inventory controllers, models, services, and views
- Map business logic locations and flows
- Document dependencies and integrations
- **Output**: `reports/Application-Discovery-Report.md`

### Phase 1: Technical Assessment (`/phase1-technicalassessment`)

Assess migration risks and gather preferences:
- Evaluate migration complexity and risks
- Map PHP patterns to Java equivalents
- Gather architecture preferences (Spring Boot MVC vs REST API, JPA/Hibernate vs JDBC)
- Estimate migration effort
- **Output**: `reports/Technical-Assessment-Report.md`

### Phase 2: Migration Planning (`/phase2-createmigrationplan`)

Create detailed file-by-file migration plan:
- Document method-level mappings for services
- Define migration order by waves (dependencies first)
- Track business rules from source to target
- **Output**: `reports/Migration-Plan-Detailed.md`

### Phase 3: Code Migration (`/phase3-migratecode`)

Execute the PHP to Java 21 migration:
- Create Java 21 / Spring Boot project structure
- Migrate controllers, services, and business logic
- Convert Eloquent models to JPA / Hibernate entities
- Transform Blade/Twig to Thymeleaf templates
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

- **PHP Framework Detection**: Automatic detection of Laravel, Symfony, CodeIgniter, Slim, Lumen, vanilla PHP
- **ORM Migration**: Eloquent/Doctrine to JPA / Hibernate with relationships
- **Template Conversion**: Blade/Twig syntax to Thymeleaf templates
- **Package Mapping**: Composer packages to Maven/Gradle equivalents
- **Authentication Modernization**: Laravel Auth to Spring Security or Entra ID
- **Containerization**: Docker support with multi-stage builds
- **Multi-Platform Support**: App Service, Container Apps, AKS
- **Status Tracking**: Comprehensive progress reporting with `/getstatus`
- **Skills System**: Specialized skills with code examples and templates

## PHP to Java 21 Mapping Quick Reference

| PHP | Java 21 |
|-----|---------|
| Laravel/Symfony | Spring Boot MVC |
| CodeIgniter/Slim/Lumen | Spring Boot REST |
| Eloquent/Doctrine | JPA / Hibernate |
| Blade/Twig | Thymeleaf templates |
| Laravel Auth | Spring Security |
| Composer packages | Maven/Gradle dependencies |
| `.env` files | `application.yml` / `application.properties` |
| Laravel Queues | Spring @Async / RabbitMQ |

For complete mapping tables with examples, see `.github/skills/php-to-java-mapping/`.

## Avoiding Hallucinations

To reduce hallucinations during migration, the guided prompts use files in `reports/`:

- `reports/Report-Status.md` — overall migration status dashboard
- `reports/Application-Discovery-Report.md` — Phase 0 output
- `reports/Technical-Assessment-Report.md` — Phase 1 output
- `reports/Migration-Plan-Detailed.md` — Phase 2 output (file-by-file plan)

During each phase, read the summary carefully to understand what will be delivered and what inputs are needed.

> **Pro tip**: Use `/getstatus` at any time to check migration progress
> **Pro tip 2**: Use the **@terminal** command to debug issues
> **Pro tip 3**: Always verify business logic is preserved after migration

## Getting Started

1. Clone this repository
2. Install [GitHub Copilot](https://copilot.github.com/) in Visual Studio Code
3. Open VS Code and select the PHP to Java 21 Migration agent
4. Start with `/phase0-applicationdiscovery` to analyze your PHP application
5. Use `/getstatus` at any time to check migration progress
6. Follow the guided prompts through all 7 phases

## Target Azure Hosting Platforms

The migration process supports multiple Azure hosting options:

- **Azure App Service**: For web applications and APIs
- **Azure Container Apps**: For containerized applications and microservices
- **Azure Kubernetes Service (AKS)**: For complex containerized applications

## Sample Application

The repository includes a PHP Bookstore application (`Sample/bookstore/`) for demonstrating the migration:

- **Source**: Vanilla PHP with MySQL
- **Features**: User registration, login, profile editing, book management
- **Database**: MySQL with Docker Compose support

Run the sample with Docker:
```bash
cd Sample
docker-compose up -d
```

Access at: http://localhost:8080

## Contributing

Contributions to improve the agents, prompts, skills, or add new features are welcome. Please feel free to submit pull requests or open issues to discuss potential improvements.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
