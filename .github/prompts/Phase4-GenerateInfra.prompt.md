---
agent: agent
model: Claude Sonnet 5 (copilot)
tools: [vscode, execute, read, browser, edit, search, web, azure/search]
---

# Phase 4: Generate Infrastructure as Code for Azure Deployment

## Objective

Generate Infrastructure as Code (IaC) files for deploying the migrated Java 21 (Spring Boot) application to Azure using the hosting platform selected in Phase 1.

**Prerequisites**:
- Phase 3: Code migration completed with working Java 21 (Spring Boot) application

---

## Step 1: Review Migration Configuration

Read the technical assessment to understand the target infrastructure:

```
read_file: reports/Technical-Assessment-Report.md
```

Confirm:
- [ ] Azure hosting platform (App Service / Container Apps / AKS)
- [ ] IaC tool preference (Bicep / Terraform)
- [ ] Database service
- [ ] Authentication approach

---

## Step 2: Generate Infrastructure

Use `azure_development-summarize_topic` tool to get high-level instructions to follow.

Use `azure_recommend_service_config` to automatically detect services and dependencies from the migrated application.

Use `azure_check_region` to validate that required Azure services are available in the target region.

Use `azure_check_quota` to ensure sufficient quota for deployment.

Create an 'infra' directory in the modernized project folder if it doesn't already exist.

Create an azure.yaml file in the root of the modernized project for Azure Developer CLI (azd) support.

Use `azure_check_predeploy` to validate the generated infrastructure files.

Use managed identities for authentication instead of connection strings and keys.

Set up proper RBAC with least privilege principles.

Configure appropriate scaling settings based on the application requirements.

Set up proper networking and security configurations including private endpoints where applicable.

Configure cost optimization settings (auto-scaling, reserved instances where appropriate).

Set up monitoring, alerting, and log aggregation.

Include infrastructure testing and validation scripts.

If infrastructure generation fails, provide detailed error analysis and alternative approaches.

Make the infrastructure section in the migration report human-readable and in markdown format, using headings, bullet points, and other formatting options as appropriate.

Suggest that the next step is to deploy to Azure, and mention `/phase5-deploytoazure` is the command to start the deployment process.

At the end, update the status report file reports/Report-Status.md with the status of the assessment step, including:
  - Infrastructure components created
  - Security configurations implemented  
  - Monitoring and logging setup
  - Any issues encountered during generation

Set up proper monitoring with Application Insights and Log Analytics.

Configure Entra ID integration for authentication with proper RBAC.

Set up database resources if applicable (Azure Database for PostgreSQL, Azure SQL, Cosmos DB, etc.) with private endpoints.

Include proper tagging and naming conventions using resource tokens.

Implement security best practices: managed identities, private endpoints, network restrictions.

Configure RBAC assignments for service-to-service authentication using managed identities.

Configure Java-specific runtime settings:
- For App Service: Set `WEBSITES_JAVA_VERSION` and `JAVA_SE_EMBEDDED_SERVER_ENABLED=true` for embedded Tomcat/Netty
- For Container Apps: Use Eclipse Temurin base images (e.g., `eclipse-temurin:21-jre-alpine`)
- For AKS: JDK 21-based container images

Configure Spring Boot environment variables:
- `SPRING_PROFILES_ACTIVE` for environment-specific configuration (replaces the ColdFusion server/environment settings from `settings.ini.cfm` / `settings.local.cfm`)
- `spring.datasource.url`, `spring.datasource.username`, `spring.datasource.password` for database connections
- `server.port=8080` for the application port

Configure health check endpoints to use Spring Boot Actuator (`/actuator/health`).

Configure Application Insights for Java using the Application Insights Java agent (`applicationinsights-agent-*.jar`).

Based on the chosen Azure hosting platform in the assessment report (App Service, AKS, or Container Apps), generate the appropriate infrastructure files:

## For Bicep Infrastructure:
- Use Azure Verified Modules (AVM) where available for best practices, https://github.com/Azure/bicep-registry-modules.
- Use `azure_bicep_schemas-get_bicep_resource_schema` tool for each resource type to ensure correct schema usage.
- Create the following structure in the 'infra' folder:
  - main.bicep - Main deployment file with proper targeting scope
  - main.parameters.json - Parameters for the deployment
  - modules/ - Folder for modular Bicep files
    - appService.bicep or containerApp.bicep or aks.bicep (depending on chosen platform)
    - monitoring.bicep - Application Insights and Log Analytics resources
    - database.bicep (if applicable) - Database resources with proper networking
    - identityAndSecurity.bicep - Managed Identity and RBAC setup
    - networking.bicep (if applicable) - VNet, NSG, private endpoints
    - keyvault.bicep - Azure Key Vault for secrets management
    - Key Vault must be configured with RBAC only (do not use access policies)
- Configure the infrastructure for the selected hosting platform:
  - For App Service: Set up App Service Plan (Java SE or Tomcat stack), App Service, deployment slots, and related resources. Configure `WEBSITES_JAVA_VERSION`, `JAVA_SE_EMBEDDED_SERVER_ENABLED`, and `SPRING_PROFILES_ACTIVE`.
  - For AKS: Set up AKS cluster, node pools, Azure Container Registry, and related resources. Use JDK 21-based container images.
  - For Container Apps: Set up Container Apps Environment, Container Registry, and Container Apps. Use Eclipse Temurin base images. Configure health probes to use `/actuator/health`.

## For Terraform Infrastructure:
- Use `mcp_azure_mcp_azureterraformbestpractices` to retrieve current Terraform best practices for Azure.
- Create the following structure in the 'infra' folder:
  - main.tf - Main deployment file
  - variables.tf - Variable definitions
  - outputs.tf - Output definitions
  - providers.tf - Provider configuration
  - modules/ - Folder for modular Terraform files
    - app_service/ or container_app/ or aks/ (depending on chosen platform)
    - monitoring/ - Application Insights and Log Analytics resources
    - database/ (if applicable) - Database resources
    - identity/ - Managed Identity and RBAC setup
    - networking/ (if applicable) - VNet, NSG, etc.
- Configure the infrastructure for the selected hosting platform.
- Set up proper monitoring with Application Insights and Log Analytics.
- Configure Entra ID integration for authentication.
- Set up database resources if applicable (Azure Database for PostgreSQL, Azure SQL, Cosmos DB, etc.).
- Include proper tagging and naming conventions.
- Prefer Managed Identity and OIDC federated credentials; avoid storing secrets in state or code.
- Configure Spring Boot datasource properties via environment variables or Key Vault references.

---

## Step 3: Validate Infrastructure

Use `azure_check_predeploy` to validate the generated infrastructure files.

---

## Step 4: Update Status Report

Update `reports/Report-Status.md`:

```markdown
## Phase 4 Summary

- **Infrastructure Status**: Complete
- **IaC Tool**: [Bicep/Terraform]
- **Target Platform**: [App Service/Container Apps/AKS]
- **Validation Status**: ✅ Passed

## Next Step

Run `/phase5-deploytoazure` to deploy the application to Azure.
```

---

## Deliverables

At the end of Phase 4:

1. ✅ `infra/` directory with all IaC files
2. ✅ `azure.yaml` for Azure Developer CLI
3. ✅ Managed identity configurations
4. ✅ Monitoring and security setup
5. ✅ Infrastructure validated
6. ✅ `reports/Report-Status.md` updated

**Next Step**: `/phase5-deploytoazure` to deploy to Azure.
