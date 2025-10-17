package de.ibmix.magkit.query.sql2.statement;

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
 * Fluent API step for declaring a JOIN in a SQL2 statement being built.
 * <p>Allows adding an INNER, LEFT OUTER or RIGHT OUTER JOIN on a second node type.
 * After selecting a join method you advance to {@link Sql2JoinAs} to assign a selector
 * name for the joined node type and then to {@link Sql2JoinOn} to define the join condition.</p>
 * <p>Preconditions: A SELECT (and optionally FROM/AS) step must have been started via {@link Sql2Statement#select(String...)}.
 * Passing a null or blank node type will result in an invalid query fragment ("[]"), therefore callers
 * should always provide a valid JCR node type name.</p>
 * <p>Thread-safety: Implementations are not thread-safe; each builder instance should be used by a single thread.</p>
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2020-05-15
 */
public interface Sql2Join extends Sql2Where {
    /**
     * Start an INNER JOIN on the given node type.
     * @param nodeType the JCR node type to join (must be non-blank)
     * @return the next builder step to assign a selector name to the joined node type
     */
    Sql2JoinAs innerJoin(String nodeType);
    /**
     * Start a LEFT OUTER JOIN on the given node type.
     * @param nodeType the JCR node type to join (must be non-blank)
     * @return the next builder step to assign a selector name to the joined node type
     */
    Sql2JoinAs leftOuterJoin(String nodeType);
    /**
     * Start a RIGHT OUTER JOIN on the given node type.
     * @param nodeType the JCR node type to join (must be non-blank)
     * @return the next builder step to assign a selector name to the joined node type
     */
    Sql2JoinAs rightOuterJoin(String nodeType);
}
