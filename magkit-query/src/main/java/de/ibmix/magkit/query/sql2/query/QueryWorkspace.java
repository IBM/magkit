package de.ibmix.magkit.query.sql2.query;

/*-
 * #%L
 * IBM iX Magnolia Kit
 * %%
 * Copyright (C) 2023 IBM iX
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

/**
 * Builder step interface for selecting the JCR workspace used to execute the query.
 * <p>Purpose: Encapsulates the mandatory choice of a workspace (e.g. Magnolia "website") early in the fluent chain to
 * ensure subsequent steps (statement, limits) operate on a defined repository context.</p>
 * <p>Key features:</p>
 * <ul>
 *   <li>Explicit workspace selection by name.</li>
 *   <li>Convenience method targeting the common Magnolia "website" workspace.</li>
 *   <li>Type parameter propagation for fluent chaining.</li>
 * </ul>
 * <p>Usage example:</p>
 * <pre>{@code Sql2QueryBuilder.forNodes().fromWebsite().withStatement("SELECT * FROM [mgnl:page]");}</pre>
 * @param <T> the concrete builder type enabling fluent chaining
 * @author wolf.bubenik@ibmix.de
 * @since 2020-04-27
 */
public interface QueryWorkspace<T> {
    /**
     * Specify the workspace by its name.
     * @param workspace JCR workspace name (must exist)
     * @return fluent builder instance
     */
    T fromWorkspace(String workspace);

    /**
     * Convenience method selecting the Magnolia "website" workspace.
     * @return fluent builder instance
     */
    T fromWebsite();
}
