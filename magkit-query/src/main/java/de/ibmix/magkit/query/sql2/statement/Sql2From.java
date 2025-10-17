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
 * SQL2 statement builder interface for fluent API: Optional from step.
 * <p>Defines the node type for the primary selector. If omitted defaults to nt:base. Blank or null values are ignored.</p>
 * <p>Thread-safety: Implementations are not thread-safe.</p>
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2020-04-15
 */
public interface Sql2From extends Sql2As {
    /**
     * Specify the JCR node type for the FROM clause (defaults to nt:base if blank or null).
     * @param nodeType the JCR node type name
     * @return next step allowing assignment of a selector name
     */
    Sql2As from(String nodeType);
}
