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
 * SQL2 statement builder interface for fluent API: Optional order direction step.
 * <p>Defaults to descending order; choose {@link #ascending()} to override. After setting the direction the
 * builder can proceed to build the statement.</p>
 * <p>Thread-safety: Implementations are not thread-safe.</p>
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2020-04-15
 */
public interface Sql2OrderDirection extends Sql2Builder {
    /**
     * Use descending order for previously defined order attributes (default).
     * @return final builder step to build the SQL2 string
     */
    Sql2Builder descending();
    /**
     * Use ascending order for previously defined order attributes.
     * @return final builder step to build the SQL2 string
     */
    Sql2Builder ascending();
}
