---
agent: 'ColdFusion to Java Migration'
model: GPT-5 mini (copilot)
tools: [vscode, execute, read, browser, edit, search, web, azure/search]
---

# Phase 6: Set Up CI/CD Pipelines

## Objective

Set up CI/CD pipelines for automated deployment and continuous integration of the migrated Java 21 (Spring Boot) application.

**Prerequisites**:
- Phase 5: Application successfully deployed to Azure

---

## Step 1: Pipeline Platform Selection
- Use `azure_config_deploymentpipeline` to generate deployment pipeline configurations.
- Use `file_search` to locate existing pipeline files and understand current CI/CD setup.
- Use `semantic_search` to identify deployment requirements from the application structure.
- Set up comprehensive CI/CD pipelines that support the target Azure platform and hosting approach.
- Create pipeline configurations that follow Azure DevOps and GitHub Actions best practices.

## CI/CD Strategy Implementation

### Pipeline Platform Selection:
- Determine whether to use GitHub Actions, Azure DevOps, or both
- Consider existing organizational preferences and integrations
- Evaluate security and compliance requirements
- Set up service connections and authentication

### For GitHub Actions:
- Create `.github/workflows/` directory structure
- Set up workflow files for:
  - Continuous Integration (CI) pipeline
  - Continuous Deployment (CD) pipeline
  - Infrastructure deployment pipeline
  - Security scanning pipeline
- Configure GitHub secrets for Azure authentication
- Set up environment protection rules
- Configure branch protection policies

### For Azure DevOps:
- Create Azure DevOps project and repository connections
- Set up build pipelines (azure-pipelines.yml)
- Configure release pipelines for deployment
- Set up service connections to Azure
- Configure variable groups and secure variables
- Set up approval processes and gates

## Pipeline Configuration Details

### Continuous Integration Pipeline:
# Include the following stages:
- Source code checkout and caching
- JDK 21 setup (use `actions/setup-java@v4` for GitHub Actions)
- Dependency resolution and caching (`mvn dependency:resolve` or Gradle cache)
- Code compilation (`mvn compile` or `gradle compileJava`)
- Code quality analysis (SonarQube, Checkstyle, SpotBugs, PMD)
- Security scanning (OWASP Dependency-Check, Snyk)
- Unit test execution with JaCoCo coverage reporting (`mvn test` or `gradle test`)
- Integration test execution
- Application packaging (`mvn package -DskipTests` or `gradle build`)
- Container image build and security scanning (if applicable)
- Artifact publishing to registry (`.jar` or `.war` from `target/` or `build/libs/`)
- Infrastructure validation (Bicep/Terraform linting)

### Continuous Deployment Pipeline:
# Include the following stages:
- Environment-specific configuration
- Infrastructure deployment (using azd or direct ARM/Bicep)
- Application deployment to staging environment
- Smoke tests and health checks
- Integration tests against staging
- Security tests and compliance validation
- Performance tests and baseline validation
- Production deployment with approval gates
- Post-deployment validation and monitoring
- Rollback procedures in case of failures

## Environment Management:

### Multi-Environment Setup:
- Configure development, staging, and production environments
- Set up environment-specific configurations and secrets
- Implement environment promotion strategies
- Configure environment isolation and security
- Set up monitoring and logging for each environment

### Infrastructure as Code Integration:
- Integrate Bicep/Terraform deployment in pipelines
- Set up infrastructure validation and testing
- Configure infrastructure drift detection
- Implement infrastructure rollback procedures
- Set up infrastructure security scanning

## Deliverables:

- Generate a CI/CD setup report in the 'reports' folder, named 'cicd_setup_report.md', including:
  - Pipeline architecture and configuration details
  - Environment setup and management procedures
  - Security and compliance integration
  - Quality gates and approval processes
  - Monitoring and observability setup
  - Performance optimization configurations
  - Operational procedures and troubleshooting guides
  - Cost optimization strategies
  - Training and documentation resources

- Create actual pipeline configuration files in the appropriate directories:
  - `.github/workflows/` for GitHub Actions
  - `azure-pipelines.yml` for Azure DevOps
  - Environment-specific configuration files
  - Security scanning configurations

- If CI/CD setup fails at any step, provide detailed error analysis and alternative approaches.
- Make the CI/CD report human-readable and in markdown format with clear sections and actionable guidance.
- Suggest that the migration and modernization process is now complete! Mention /getstatus to review the final status and next steps for ongoing maintenance and optimization.
- At the end, update the status report file reports/Report-Status.md with the status of the CI/CD step and mark the overall migration process as successfully completed.

---

## Step 5: Update Status Report

Update `reports/Report-Status.md`:

```markdown
## Phase 6 Summary

- **CI/CD Status**: Complete
- **Pipeline Platform**: [GitHub Actions/Azure DevOps]
- **Pipelines Created**: ✅ CI + CD

## Overall Migration Status: ✅ COMPLETE

All phases of the ColdFusion to Java 21 migration have been successfully completed!

| Phase | Status |
|-------|--------|
| Phase 0: Application Discovery | ✅ Complete |
| Phase 1: Technical Assessment | ✅ Complete |
| Phase 2: Migration Planning | ✅ Complete |
| Phase 3: Code Migration | ✅ Complete |
| Phase 4: Infrastructure | ✅ Complete |
| Phase 5: Deployment | ✅ Complete |
| Phase 6: CI/CD Setup | ✅ Complete |
```

---

## Deliverables

At the end of Phase 6:

1. ✅ CI/CD pipelines configured
2. ✅ Pipeline configuration files created
3. ✅ `reports/cicd_setup_report.md` generated
4. ✅ `reports/Report-Status.md` updated
5. ✅ Migration process complete!

**Next Step**: Run `/getstatus` to review the final status and recommendations for ongoing maintenance.
