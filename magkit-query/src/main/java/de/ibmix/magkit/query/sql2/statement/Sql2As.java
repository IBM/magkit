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
 * Fluent API step for assigning a selector name to the primary (FROM) node type.
 * <p>Selector names allow referencing attributes and functions scoped to a particular selector
 * especially in JOIN or PATH conditions. If omitted, attributes are output without selector prefix.</p>
 * <p>Null/blank handling: A null or blank value results in no selector name being stored.</p>
 * <p>Thread-safety: Not thread-safe.</p>
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2020-05-15
 */
public interface Sql2As extends Sql2Where {
    /**
     * Assign a selector name to the primary FROM node type.
     * @param selectorName the selector alias (may be null/blank to skip)
     * @return next step enabling joins and where clauses
     */
    Sql2Join as(String selectorName);
}
