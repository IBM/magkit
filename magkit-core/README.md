# magkit-core

## Overview
`magkit-core` provides foundational utilities and abstractions to speed up Magnolia CMS module development. It focuses on:

- JCR Node wrappers (`ImmutableNodeWrapper`, `AlteringNodeWrapper`, `FallbackNodeWrapper`) to simplify read-only access, on-the-fly alterations, and fallback value resolution.
- Property stubbing (`StubbingProperty`) for dynamic value injection.
- HTTP and rendering helpers (e.g. `CacheUtils` to disable caching, `SelectorUtils` for URL selector handling like paging or print views).
- Authoring enhancements (`ExtendedLinkFieldHelper`) for robust link field resolution and normalization.
- Internationalization helpers (`LocaleUtil`) for locale discovery, filtering and Magnolia i18n configuration access.
- Encoding helpers (`EncodingUtils`) for common string and URL encoding/decoding tasks.
- Lightweight data holders (`Item`) with sortable key/value semantics.

Together these utilities reduce boilerplate when interacting with Magnolia's JCR, request/response layers, and authoring UI components.

## Usage
Add the dependency to your Maven project. Either use the parent BOM `magkit` or declare the module directly.

### Option 1: Import the parent (recommended for multiple magkit modules)
```xml
<parent>
    <groupId>de.ibmix.magkit</groupId>
    <artifactId>magkit-pom</artifactId>
    <version>1.1.0</version><!-- or a newer released version -->
</parent>
```
Then reference the module:
```xml
<dependencies>
    <dependency>
        <groupId>de.ibmix.magkit</groupId>
        <artifactId>magkit-core</artifactId>
        <version>${project.version}</version>
    </dependency>
</dependencies>
```

### Option 2: Direct dependency
```xml
<dependency>
    <groupId>de.ibmix.magkit</groupId>
    <artifactId>magkit-core</artifactId>
    <version>1.1.0</version><!-- use a released version; SNAPSHOT only if you need latest development -->
</dependency>
```

### Magnolia compatibility
Tested against Magnolia 6.3.x (see parent POM for exact versions). Ensure your Magnolia bundle provides matching core and enterprise artifacts.

## Examples
Below are short examples illustrating typical features.

### Disable HTTP caching for a response
```java
import de.ibmix.magkit.core.utils.CacheUtils;

// In a servlet filter or rendering model
CacheUtils.preventCaching(response); // Sets Cache-Control, Pragma and Expires headers
```

### Work with URL selectors (e.g. paging)
```java
import de.ibmix.magkit.core.utils.SelectorUtils;
import info.magnolia.context.MgnlContext;

String selector = SelectorUtils.getSelector(MgnlContext.getAggregationState());
int page = SelectorUtils.getPageNumber(selector, 1); // fallback to page 1 if absent
boolean printView = SelectorUtils.isPrint(selector);
```

### Resolve locales
```java
import de.ibmix.magkit.core.utils.LocaleUtil;

// All configured locales from Magnolia i18n
List<Locale> supported = LocaleUtil.getConfiguredLocales();
Locale defaultLocale = LocaleUtil.getDefaultLocale();
Locale requested = LocaleUtil.findBestMatchingLocale("en_US", supported, defaultLocale);
```

### Immutable node access
```java
import de.ibmix.magkit.core.node.ImmutableNodeWrapper;
import javax.jcr.Node;

ImmutableNodeWrapper view = new ImmutableNodeWrapper(jcrNode);
String title = view.getProperty("title").getString(); // safe read-only view
```

### Fallback node wrapper
```java
import de.ibmix.magkit.core.node.FallbackNodeWrapper;
import javax.jcr.Node;

// Provide a primary node and a fallback node for missing properties
FallbackNodeWrapper decorated = new FallbackNodeWrapper(primaryNode, fallbackNode);
String teaser = decorated.getProperty("teaser").getString(); // uses fallback if absent on primary
```

### Extended link field normalization
```java
import de.ibmix.magkit.core.utils.ExtendedLinkFieldHelper;
import info.magnolia.objectfactory.Components;

ExtendedLinkFieldHelper helper = Components.getComponent(ExtendedLinkFieldHelper.class);
String normalized = helper.normalizeLink(rawAuthorLinkValue); // cleans & resolves link types
```

### Stub a property for testing
```java
import de.ibmix.magkit.core.node.StubbingProperty;
import javax.jcr.Value;

StubbingProperty property = new StubbingProperty("title", session.getValueFactory().createValue("Hello"));
String value = property.getString();
```

## License
Apache License 2.0. You may obtain a copy of the License at:

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

## Authors
- Frank Sommer (frank.sommer@ibmix.de)
- Wolf Bubenik (wolf.bubenik@ibmix.de)

Contributions welcome. Please see CONTRIBUTING.md in the repository root.

