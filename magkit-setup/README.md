# magkit-setup Module

## Overview
`magkit-setup` streamlines initial provisioning and iterative updates of Magnolia CMS instances. It focuses on declarative, repeatable, and safe module installation/update tasks and supporting utilities.

### Main Features
- Version Handler Extension (`BootstrapModuleVersionHandler`): Central place to aggregate install and update deltas; simplifies adding custom tasks for fresh installs and upgrades.
- Delta Tasks for Repository Maintenance:
  - `InstallBootstrapTask` / `ModuleInstanceBootstrapTask`: Controlled bootstrapping of YAML/XML resources with filtering logic (public/author, extension acceptance).
  - `RemoveResourcesHotfixesTask`: Cleans up obsolete or hotfixed resource files from previous releases.
  - `ReplaceTemplateTask`: Replaces template definition nodes while preserving structure; generates descriptive task names automatically.
  - `RemoveTemplateNodesTask`: Bulk-removes content nodes matching template names (optionally scoped by workspace/path) using dynamically built SQL2 queries.
  - `ScriptInstallTask`: Executes custom scripted install logic (e.g. Groovy/JavaScript) during module setup (intended for advanced migration scenarios).
- Node Builder Utilities:
  - `NodeOperationFactory`: Fluent factory for Magnolia's `NodeOperation` (add/get nodes, ordering, properties, removal, URI pattern voters).
  - `NodeBuilderTask` / `NodeBuilderTaskFactory`: Wrap sequences of node operations inside install/update tasks, targeting config, server, or module-specific paths with error handling.
  - `TaskLogErrorHandler`: Simple error handler wiring Magnolia task errors into install context logs.
- Workflow Automation:
  - `AutoApproveHumanTaskWorkItemHandler` (+ Definition): JBoss jBPM work item handler that auto-approves human tasks to accelerate simple workflow chains (e.g. automatic publication).
- Virtual URI Mapping:
  - `VersionNumberVirtualUriMapping`: Rewrites versioned request URIs (e.g. `/v1.2.3/docs/...`) to canonical targets, validating semantic version patterns and prefix rules.
- Security & Authoring:
  - `AuthorFormClientCallback`: Custom client callback to adapt author login/form behavior based on Magnolia configuration properties.

Together these components reduce manual effort building robust installation scripts and upgrade paths, help enforce consistent configuration, and accelerate workflow-driven publishing in Magnolia projects.

## Usage
Add the dependency to your Maven project either via the parent or directly.

### Option 1: Use parent aggregation (recommended when using multiple magkit modules)
```xml
<parent>
  <groupId>de.ibmix.magkit</groupId>
  <artifactId>magkit</artifactId>
  <version>1.1.0</version><!-- use a released version -->
</parent>

<dependencies>
  <dependency>
    <groupId>de.ibmix.magkit</groupId>
    <artifactId>magkit-setup</artifactId>
    <version>${project.version}</version>
  </dependency>
</dependencies>
```

### Option 2: Direct dependency
```xml
<dependency>
  <groupId>de.ibmix.magkit</groupId>
  <artifactId>magkit-setup</artifactId>
  <version>1.1.0</version><!-- prefer a released version over SNAPSHOT for production -->
</dependency>
```

### Magnolia Compatibility
Designed for Magnolia 6.x; verify exact tested versions in the root parent POM. Ensure required Magnolia bundles (core, rendering, workflow) are present.

## Examples
Short examples illustrating typical usage patterns.

### 1. Adding a Custom Delta in the Version Handler
Extend `BootstrapModuleVersionHandler` (or modify it) to inject a template replacement during update:
```java
@Override
protected List<Task> getUpdateDeltas(InstallContext ctx, Version from) {
  List<Task> tasks = new ArrayList<>(super.getUpdateDeltas(ctx, from));
  tasks.add(new ReplaceTemplateTask(
      "website", // workspace
      "/modules/myModule/templates/pages", // path to template node
      "myModule:oldPage", // old template id
      "myModule:newPage"  // new template id
  ));
  return tasks;
}
```

### 2. Bulk Remove Nodes by Template
Remove all nodes using a deprecated template below a subtree:
```java
Task removeDeprecated = new RemoveTemplateNodesTask(
    "website",
    "myModule:deprecatedComponent",
    "/root/site/legacy" // scope path
);
// Add 'removeDeprecated' to install/update task list.
```

### 3. Node Builder Task for Config Adjustments
Using the factory and operations to ensure a property exists and order a node:
```java
NodeBuilderTask task = NodeBuilderTaskFactory.selectConfig(
    "/server/filters", // base config path
    "magkit-setup adjustments",
    NodeOperationFactory.addOrGetNode("cacheControl", "mgnl:contentNode"),
    NodeOperationFactory.addOrSetProperty("cacheControl/enabled", true),
    NodeOperationFactory.orderBefore("cacheControl", "security")
);
```

### 4. Auto-Approve Workflow Human Tasks
Register the handler definition via Magnolia configuration (YAML example):
```yaml
/workflows/handlers/autoApprove:
  class: de.ibmix.magkit.setup.workflow.AutoApproveHumanTaskWorkItemHandlerDefinition
```
All human tasks routed through this handler will be immediately approved, shortening publication chains.

### 5. Version Number Virtual URI Mapping
Configure a mapping to rewrite versioned docs URLs:
```yaml
/server/virtualUriMappings/versionedDocs:
  class: de.ibmix.magkit.setup.urimapping.VersionNumberVirtualUriMapping
  pattern: /v([0-9]+\.[0-9]+\.[0-9]+)/docs/(.*)
  level: 1
  fromPrefix: /v
  toUri: /docs/$2
```
Requests like `/v1.2.3/docs/intro.html` resolve to `/docs/intro.html`.

### 6. Simple Workflow Auto-Publish Setup
Use `StandardTasks.setSimpleWorkflow()` inside an install delta to convert to a simplified auto-publish workflow:
```java
List<Task> installTasks = new ArrayList<>();
installTasks.add(StandardTasks.setSimpleWorkflow());
installTasks.add(StandardTasks.setSecurityCallback());
```

### 7. Resource Bootstrapping Filter
`ModuleInstanceBootstrapTask` only accepts resources matching configured criteria (e.g. author vs public). Example of acceptance logic within a custom task:
```java
ModuleInstanceBootstrapTask bootstrapTask = new ModuleInstanceBootstrapTask();
boolean accept = bootstrapTask.acceptResource(ctx, "/mgnl-bootstrap/public/myModule/pages/home.yaml");
```

## License
Apache License, Version 2.0. See the root `LICENSE` file or visit:
http://www.apache.org/licenses/LICENSE-2.0

Distributed on an "AS IS" basis without warranties or conditions of any kind.

## Authors
- Frank Sommer (frank.sommer@ibmix.de)
- Wolf Bubenik (wolf.bubenik@ibmix.de)

Contributions are welcome. Please open issues or pull requests following the repository contribution guidelines.

