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
 * Accessor interface exposing the selector names used in a built SQL2 statement.
 * <p>The primary selector name originates from the {@link Sql2As#as(String)} step; the join selector
 * from {@link Sql2JoinAs#joinAs(String)}. Implementations return null if a selector name was not provided.</p>
 * <p>Usage: Mainly intended for downstream processing (e.g. condition objects needing selector context).</p>
 * <p>Thread-safety: Implementations are not thread-safe.</p>
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2020-05-18
 */
public interface Sql2SelectorNames {
    /**
     * Get the selector name for the FROM part or null if none was supplied.
     * @return the primary selector name or null
     */
    String getFromSelectorName();
    /**
     * Get the selector name for the JOIN part or null if none was supplied.
     * @return the join selector name or null
     */
    String getJoinSelectorName();
}
