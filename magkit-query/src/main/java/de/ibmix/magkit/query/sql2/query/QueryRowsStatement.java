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
 * Builder step interface declaring methods for specifying the SQL2 statement of a row-oriented query.
 * <p>Purpose: Adds either a raw SQL2 string or a {@link Sql2Builder} instance to the fluent sequence constructing a
 * {@code RowsQueryBuilder}. This separates statement definition from execution/limit configuration steps.</p>
 * <p>Key features:</p>
 * <ul>
 *   <li>Dual input: raw SQL2 string or structured builder.</li>
 *   <li>Type parameter ensures fluent builder type propagation.</li>
 * </ul>
 * <p>Usage example:</p>
 * <pre>{@code Sql2QueryBuilder.forRows().fromWebsite().withStatement("SELECT * FROM [mgnl:page]");}</pre>
 * @param <T> the concrete builder type enabling fluent chaining
 * @author wolf.bubenik@ibmix.de
 * @since 2020-04-28
 */
public interface QueryRowsStatement<T> {
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
