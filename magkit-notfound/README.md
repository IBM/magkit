# magkit-notfound

## Overview
`magkit-notfound` is a Magnolia CMS extension module providing flexible, site-aware error page handling. It centralizes resolution of HTTP error pages (notably 404, but also other 4xx/5xx codes) and exposes both classic page forwarding and headless JSON representations.

### Main Features
- Site-aware error page resolution using Magnolia site mappings and domain / original URI context.
- Configurable default error page path and relative error path segment (e.g. `error`).
- HTTP status-to-page name mapping (e.g. map `404` to `not-found`, `500` to `system-error`).
- Automatic existence check of candidate error page nodes in the `website` workspace.
- Forward-based HTML rendering (`/error/default`) for traditional page delivery.
- Headless JSON endpoint (`/error/headless`) for API / SPA consumption.
- JAX-RS `ExceptionMapper` integration translating `PageRenderingException` into structured error entities.
- Simple error entity structure for easy serialization: `{ "status": <int>, "page": "<resolvedPath>" }`.

## Usage
Add the Maven dependency to your project. The module inherits the version from the parent BOM.

```xml
<dependency>
  <groupId>de.ibmix.magkit</groupId>
  <artifactId>magkit-notfound</artifactId>
  <version>1.1.0</version><!-- or the latest released version, e.g. 1.0.2 -->
</dependency>
```

### Module Configuration
Configure the module via Magnolia (e.g. YAML / JCR) to set:
- `defaultErrorPath`: fallback path if no site mapping resolves (e.g. `/global/error`).
- `relativeErrorPath`: appended segment under site path (defaults to `error`).
- `errorCodeMapping`: map of status codes to page names `{ "404": "not-found", "500": "system-error" }`.

Example (conceptual YAML):
```yaml
modules:
  magkit-notfound:
    config:
      defaultErrorPath: /global/error
      relativeErrorPath: error
      errorCodeMapping:
        404: not-found
        500: system-error
```

## Examples
### 1. Resolve an error page path programmatically
```java
@Inject
private ErrorService errorService;

public String resolve404(String domain, String originalUri) {
    return errorService.retrieveErrorPagePath(404, domain, originalUri);
}
```

### 2. Produce a headless error entity
```java
Map<String, Object> entity = errorService.createEntity(404, "/site-a/section/page");
// entity => {"status":404, "page":"/site-a/error/not-found"}
```

### 3. JAX-RS mapping of page rendering failures
When a page cannot be rendered:
```java
throw new PageRenderingException(404, "/site-a/missing/page");
```
The `PageRenderingErrorMapping` automatically converts this into a response:
```json
{"status":404, "page":"/site-a/error/not-found"}
```

### 4. Web.xml error forwarding
```xml
<error-page>
  <error-code>404</error-code>
  <location>/magnoliaError/error/default</location>
</error-page>
```

### 5. Headless endpoint (JSON)
```bash
curl -s https://your-host/magnoliaError/error/headless | jq
# {
#   "status": 404,
#   "page": "/site-a/error/not-found"
# }
```

### 6. Custom mapping for 500 errors
```java
Map<String, String> mapping = Map.of("404", "not-found", "500", "system-error");
NotfoundModule module = new NotfoundModule();
module.setErrorCodeMapping(mapping);
module.setDefaultErrorPath("/global/error");
module.setRelativeErrorPath("error");
```

## License
Licensed under the Apache License, Version 2.0. See the root [`LICENSE`](../LICENSE) file or visit: http://www.apache.org/licenses/LICENSE-2.0

## Authors
- Frank Sommer (IBM iX)
- Wolf Bubenik (IBM iX)

Contributions welcome. Please refer to the root `CONTRIBUTING.md` for guidelines.

