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

import de.ibmix.magkit.query.sql2.statement.Sql2Builder;

/**
 * Builder step interface declaring methods for specifying the SQL2 statement of a node-oriented query.
 * <p>Purpose: Adds the textual or programmatic (via {@link Sql2Builder}) representation of the JCR-SQL2 statement to
 * a fluent builder chain targeting {@code NodesQueryBuilder}. Ensures that subsequent terminal steps can compile the
 * statement into a JCR {@link javax.jcr.query.Query}.</p>
 * <p>Key features:</p>
 * <ul>
 *   <li>Support for both raw SQL2 string and structured {@link Sql2Builder} input.</li>
 *   <li>Type parameter preserves concrete builder type for seamless fluent chaining.</li>
 * </ul>
 * <p>Usage example:</p>
 * <pre>{@code Sql2QueryBuilder.forNodes().fromWebsite().withStatement("SELECT * FROM [mgnl:page]");}</pre>
 * @param <T> the concrete builder type enabling fluent chaining
 * @author wolf.bubenik@ibmix.de
 * @since 2020-04-28
 */
public interface QueryNodesStatement<T> {
    /**
     * Provide a programmatic SQL2 statement builder.
     * @param statementBuilder builder responsible for generating the final SQL2 string
     * @return fluent builder instance
     */
    T withStatement(Sql2Builder statementBuilder);

    /**
     * Provide a raw SQL2 statement string.
     * @param sql2 JCR-SQL2 statement (must be valid for later execution)
     * @return fluent builder instance
     */
    T withStatement(String sql2);
}
